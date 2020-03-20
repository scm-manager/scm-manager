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
    
package sonia.scm.api.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Force json output for uploads, because of a bug we have to use a html
 * mimetype for uploads.
 *
 * TODO find a better way
 * @author Sebastian Sdorra
 */
@Provider
public class RestActionUploadResultMessageWriter
  implements MessageBodyWriter<RestActionResult>
{

  /**
   * Method description
   *
   *
   * @param result
   * @param type
   * @param genericType
   * @param annotations
   * @param mediaType
   * @param httpHeaders
   * @param entityStream
   *
   * @throws IOException
   * @throws WebApplicationException
   */
  @Override
  public void writeTo(RestActionResult result, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
    throws IOException, WebApplicationException
  {
    String v =
      "{\"success\": ".concat(String.valueOf(result.isSuccess())).concat("}");

    entityStream.write(v.getBytes(Charsets.UTF_8));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param result
   * @param type
   * @param genericType
   * @param annotations
   * @param mediaType
   *
   * @return
   */
  @Override
  public long getSize(RestActionResult result, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return -1;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param genericType
   * @param annotations
   * @param mediaType
   *
   * @return
   */
  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return RestActionUploadResult.class.isAssignableFrom(type)
      && mediaType.equals(MediaType.TEXT_HTML_TYPE);
  }
}
