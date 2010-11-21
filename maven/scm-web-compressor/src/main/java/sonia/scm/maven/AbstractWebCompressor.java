/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sonia.scm.util.ChecksumUtil;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractWebCompressor implements WebCompressor
{

  /**
   * Method description
   *
   *
   * @param document
   * @param path
   */
  protected abstract void appendElement(Document document, String path);

  /**
   * Method description
   *
   *
   * @param source
   * @param target
   * @param encoding
   *
   * @throws IOException
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  protected abstract void compress(File source, File target, String encoding)
          throws IOException, MojoExecutionException, MojoFailureException;

  /**
   * Method description
   *
   *
   * @param document
   *
   * @return
   */
  protected abstract Elements selectElements(Document document);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getExtension();

  /**
   * Method description
   *
   *
   * @param inputDirectory
   * @param element
   *
   * @return
   */
  protected abstract File getFile(File inputDirectory, Element element);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param document
   * @param inputDirectory
   * @param outputDirectory
   * @param encoding
   * @param outputPrefix
   * @param concat
   *
   * @throws IOException
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void compress(Document document, File inputDirectory,
                       File outputDirectory, String encoding,
                       String outputPrefix, boolean concat)
          throws IOException, MojoExecutionException, MojoFailureException
  {
    Elements elements = selectElements(document);

    if ((elements != null) &&!elements.isEmpty())
    {
      if (concat)
      {
        File uncompressedFile = concat(elements, inputDirectory);

        compress(document, encoding, outputDirectory, outputPrefix,
                 uncompressedFile);
      }
      else
      {
        for (Element element : elements)
        {
          File uncompressedFile = getFile(inputDirectory, element);

          compress(document, encoding, outputDirectory, outputPrefix,
                   uncompressedFile);
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param source
   * @param target
   *
   * @throws IOException
   */
  private void append(File source, File target) throws IOException
  {
    FileInputStream input = null;
    FileOutputStream output = null;

    try
    {
      input = new FileInputStream(source);
      output = new FileOutputStream(target, true);
      IOUtil.copy(input, output);
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(output);
    }
  }

  /**
   * Method description
   *
   *
   * @param document
   * @param encoding
   * @param outputDirectory
   * @param outputPrefix
   * @param uncompressedFile
   *
   * @throws IOException
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  private void compress(Document document, String encoding,
                        File outputDirectory, String outputPrefix,
                        File uncompressedFile)
          throws IOException, MojoExecutionException, MojoFailureException
  {
    File compressedFile = File.createTempFile("scm-", ".compressed");

    compress(uncompressedFile, compressedFile, encoding);
    IOUtil.delete(uncompressedFile);

    String checksum = ChecksumUtil.createChecksum(compressedFile);
    String name = checksum.concat(".").concat(getExtension());
    File scriptFile = new File(outputDirectory, name);

    compressedFile.renameTo(scriptFile);

    if (!scriptFile.exists())
    {

      // TODO copy and remove
      throw new IOException("could not move ".concat(compressedFile.getPath()));
    }

    StringBuilder path = new StringBuilder(outputPrefix);

    if (!outputPrefix.endsWith("/"))
    {
      path.append("/");
    }

    path.append(name);
    appendElement(document, path.toString());
  }

  /**
   * Method description
   *
   *
   * @param elements
   * @param inputDirectory
   *
   * @return
   *
   * @throws IOException
   */
  private File concat(Elements elements, File inputDirectory) throws IOException
  {
    File tempFile = File.createTempFile("scm-", ".concat");

    for (Element scriptEl : elements)
    {
      File file = getFile(inputDirectory, scriptEl);

      if (file.exists())
      {
        append(file, tempFile);
        scriptEl.remove();
      }
    }

    return tempFile;
  }
}
