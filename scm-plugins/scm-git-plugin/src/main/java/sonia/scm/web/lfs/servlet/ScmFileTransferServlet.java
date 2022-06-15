/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.web.lfs.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.lfs.errors.CorruptLongObjectException;
import org.eclipse.jgit.lfs.errors.InvalidLongObjectIdException;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.Constants;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lfs.server.LfsProtocolServlet;
import org.eclipse.jgit.lfs.server.fs.FileLfsServlet;
import org.eclipse.jgit.lfs.server.internal.LfsServerText;
import org.eclipse.jgit.util.HttpSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.util.IOUtil;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

/**
 * This Servlet provides the upload and download of files via git-lfs.
 * <p>
 * This implementation is based on {@link FileLfsServlet} but adjusted to work with
 * servlet-2.5 instead of servlet-3.1.
 * <p>
 *
 * @see FileLfsServlet
 * @since 1.54
 * Created by omilke on 15.05.2017.
 */
public class ScmFileTransferServlet extends HttpServlet {

  private static final Logger logger = LoggerFactory.getLogger(ScmFileTransferServlet.class);

  private static final long serialVersionUID = 1L;

  /**
   * Gson is used because the implementation was based on the jgit implementation. However the {@link LfsProtocolServlet} (which we do use in
   * {@link ScmLfsProtocolServlet}) also uses Gson, which currently ties us to Gson anyway.
   */
  private static Gson gson = createGson();

  private final BlobStore blobStore;

  public ScmFileTransferServlet(BlobStore store) {

    this.blobStore = store;
  }


  /**
   * Extracts the part after the last slash from path.
   *
   * @return Returns {@code null} if the part after the last slash is itself {@code null} or if its length is not 64.
   */
  @VisibleForTesting
  static String objectIdFromPath(String info) {

    int lastSlash = info.lastIndexOf('/');
    String potentialObjectId = info.substring(lastSlash + 1);

    if (potentialObjectId.length() != 64) {
      return null;

    } else {
      return potentialObjectId;
    }
  }

  /**
   * Logs the message and provides it to the client.
   *
   * @param response The response
   * @param status   The HTTP Status Code to be provided to the client.
   * @param message  the message to used for server-side logging. It is also provided to the client.
   */
  private static void sendErrorAndLog(HttpServletResponse response, int status, String message) throws IOException {

    logger.warn("Error occurred during git-lfs file transfer: {}", message);

    sendError(response, status, message);
  }

  /**
   * Logs the exception and provides only the message of the exception to the client.
   *
   * @param response  The response
   * @param status    The HTTP Status Code to be provided to the client.
   * @param exception An exception to used for server-side logging.
   */
  private static void sendErrorAndLog(HttpServletResponse response, int status, Exception exception) throws IOException {

    logger.warn("Error occurred during git-lfs file transfer.", exception);
    String message = exception.getMessage();


    sendError(response, status, message);
  }

  private static void sendError(HttpServletResponse response, int status, String message) throws IOException {

    try (PrintWriter writer = response.getWriter()) {

      gson.toJson(new Error(message), writer);

      response.setStatus(status);
      writer.flush();
    }

    response.flushBuffer();
  }

  private static Gson createGson() {

    GsonBuilder gb = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().disableHtmlEscaping();
    return gb.create();
  }

  /**
   * Provides a blob to download.
   * <p>
   * Actual implementation is based on <code>org.eclipse.jgit.lfs.server.fs.ObjectDownloadListener</code> and adjusted
   * to non-async as we're currently on servlet-2.5.
   *
   * @param request  servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    AnyLongObjectId objectId = getObjectToTransfer(request, response);
    if (objectId == null) {

      logInvalidObjectId(request.getRequestURI());
    } else {

      final String objectIdName = objectId.getName();
      logger.trace("---- providing download for LFS-Oid: {}", objectIdName);

      Blob savedBlob = blobStore.get(objectIdName);
      if (isBlobPresent(savedBlob)) {

        logger.trace("----- Object {}: providing {} bytes", objectIdName, savedBlob.getSize());
        writeBlobIntoResponse(savedBlob, response);
      } else {

        sendErrorAndLog(response, HttpStatus.SC_NOT_FOUND, MessageFormat.format(LfsServerText.get().objectNotFound, objectIdName));
      }
    }
  }

  /**
   * Receives a blob from an upload.
   * <p>
   * Actual implementation is based on <code>org.eclipse.jgit.lfs.server.fs.ObjectUploadListener</code> and adjusted
   * to non-async as we're currently on servlet-2.5.
   *
   * @param request  servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    AnyLongObjectId objectId = getObjectToTransfer(request, response);
    if (objectId == null) {

      logInvalidObjectId(request.getRequestURI());
    } else {

      logger.trace("---- receiving upload for LFS-Oid: {}", objectId.getName());
      readBlobFromResponse(request, response, objectId);
    }
  }

  /**
   * Extracts the {@link LongObjectId} from the request. Finishes the request, in case the {@link LongObjectId} cannot
   * be extracted with an appropriate error.
   *
   * @throws IOException Thrown if the response could not be completed in an error case.
   */
  private AnyLongObjectId getObjectToTransfer(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String path = request.getPathInfo();

    String objectIdFromPath = objectIdFromPath(path);
    if (objectIdFromPath == null) {

      //ObjectId is not retrievable from URL
      sendErrorAndLog(response, HttpStatus.SC_UNPROCESSABLE_ENTITY, MessageFormat.format(LfsServerText.get().invalidPathInfo, path));
      return null;
    } else {
      try {
        return LongObjectId.fromString(objectIdFromPath);
      } catch (InvalidLongObjectIdException e) {

        sendErrorAndLog(response, HttpStatus.SC_UNPROCESSABLE_ENTITY, e);
        return null;
      }
    }
  }

  private void logInvalidObjectId(String requestURI) {

    logger.warn("---- could not extract Oid from Request. Path seems to be invalid: {}", requestURI);
  }

  private boolean isBlobPresent(Blob savedBlob) {

    return savedBlob != null && savedBlob.getSize() >= 0;
  }

  private void writeBlobIntoResponse(Blob savedBlob, HttpServletResponse response) throws IOException {

    try (ServletOutputStream responseOutputStream = response.getOutputStream();
         InputStream savedBlobInputStream = savedBlob.getInputStream()) {

      response.addHeader(HttpSupport.HDR_CONTENT_LENGTH, String.valueOf(savedBlob.getSize()));
      response.setContentType(Constants.HDR_APPLICATION_OCTET_STREAM);

      IOUtil.copy(savedBlobInputStream, responseOutputStream);
    } catch (IOException ex) {

      sendErrorAndLog(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, ex);
    }

  }

  private void readBlobFromResponse(HttpServletRequest request, HttpServletResponse response, AnyLongObjectId objectId) throws IOException {
    Blob blob = blobStore.create(objectId.getName());
    try (OutputStream blobOutputStream = blob.getOutputStream();
         DigestInputStream requestInputStream = new DigestInputStream(request.getInputStream(), MessageDigest.getInstance("SHA-256"))) {

      IOUtil.copy(requestInputStream, blobOutputStream);
      validateStoredFile(blob, requestInputStream);
      blob.commit();

      response.setContentType(Constants.CONTENT_TYPE_GIT_LFS_JSON);
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (CorruptLongObjectException | IOException | NoSuchAlgorithmException ex) {
      blobStore.remove(blob);
      sendErrorAndLog(response, HttpStatus.SC_BAD_REQUEST, ex);
    }
  }

  private void validateStoredFile(Blob blob, DigestInputStream requestInputStream) throws IOException {
    byte[] digest = requestInputStream.getMessageDigest().digest();
    if (!blob.getId().equals(Hex.encodeHexString(digest, true))) {
      throw new IOException("Transferred file seems to be corrupt");
    }
  }

  /**
   * Used for providing an error message.
   */
  private static class Error {
    String message;

    Error(String m) {

      this.message = m;
    }
  }

}


