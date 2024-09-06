/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.rest;


import com.google.common.base.Charsets;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Force json output for uploads, because of a bug we have to use a html
 * mimetype for uploads.
 *
 * TODO find a better way
 */
@Provider
public class RestActionUploadResultMessageWriter
  implements MessageBodyWriter<RestActionResult>
{
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

  @Override
  public long getSize(RestActionResult result, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return -1;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return RestActionUploadResult.class.isAssignableFrom(type)
      && mediaType.equals(MediaType.TEXT_HTML_TYPE);
  }
}
