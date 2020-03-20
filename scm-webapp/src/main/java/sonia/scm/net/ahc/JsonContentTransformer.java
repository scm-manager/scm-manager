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
    
package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import com.google.common.io.ByteSource;

import sonia.scm.plugin.Extension;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

/**
 * {@link ContentTransformer} for json. The {@link JsonContentTransformer} uses
 * jacksons {@link ObjectMapper} to marshalling/unmarshalling.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
@Extension
public class JsonContentTransformer implements ContentTransformer
{

  /**
   * Constructs a new {@link JsonContentTransformer}.
   *
   */
  public JsonContentTransformer()
  {
    this.mapper = new ObjectMapper();

    // allow jackson and jaxb annotations
    AnnotationIntrospector jackson = new JacksonAnnotationIntrospector();
    AnnotationIntrospector jaxb = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());

    this.mapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(jackson, jaxb));

    // do not fail on unknown json properties
    this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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

  //~--- get methods ----------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  /** object mapper */
  private final ObjectMapper mapper;
}
