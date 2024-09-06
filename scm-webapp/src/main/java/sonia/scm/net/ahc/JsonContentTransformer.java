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

package sonia.scm.net.ahc;


import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import com.google.common.io.ByteSource;
import jakarta.ws.rs.core.MediaType;
import sonia.scm.plugin.Extension;
import sonia.scm.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link ContentTransformer} for json. The {@link JsonContentTransformer} uses
 * jacksons {@link ObjectMapper} to marshalling/unmarshalling.
 *
 * @since 1.46
 */
@Extension
public class JsonContentTransformer implements ContentTransformer
{
  private final ObjectMapper mapper;

  public JsonContentTransformer()
  {
    this.mapper = new ObjectMapper();

    // allow jackson and jaxb annotations
    AnnotationIntrospector jackson = new JacksonAnnotationIntrospector();
    AnnotationIntrospector jaxb = new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance());

    this.mapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(jackson, jaxb));

    // do not fail on unknown json properties
    this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


 
  @Override
  public ByteSource marshall(Object object)
  {
    ByteSource source = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      mapper.writeValue(baos, object);
      source = ByteSource.wrap(baos.toByteArray());
    }
    catch (IOException ex)
    {
      throw new ContentTransformerException("could not marshall object", ex);
    }

    return source;
  }

 
  @Override
  public <T> T unmarshall(Class<T> type, ByteSource content)
  {
    T object = null;
    InputStream stream = null;

    try
    {
      stream = content.openBufferedStream();
      object = mapper.readValue(stream, type);
    }
    catch (IOException ex)
    {
      throw new ContentTransformerException("could not unmarshall content", ex);
    }
    finally
    {
      IOUtil.close(stream);
    }

    return object;
  }


  /**
   * Returns {@code true}, if the content type is compatible with
   * application/json.
   *
   *
   * @param type object type
   * @param contentType content type
   *
   * @return {@code true}, if the content type is compatible with
   *   application/json
   */
  @Override
  public boolean isResponsible(Class<?> type, String contentType)
  {
    return MediaType.valueOf(contentType).isCompatible(MediaType.APPLICATION_JSON_TYPE);
  }

}
