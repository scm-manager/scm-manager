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

import com.google.common.io.ByteSource;
import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXB;
import sonia.scm.plugin.Extension;
import sonia.scm.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//~--- JDK imports ------------------------------------------------------------

/**
 * {@link ContentTransformer} for xml. The {@link XmlContentTransformer} uses 
 * jaxb to marshalling/unmarshalling.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
@Extension
public class XmlContentTransformer implements ContentTransformer
{

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
      JAXB.marshal(object, baos);
      source = ByteSource.wrap(baos.toByteArray());
    }
    catch (DataBindingException ex)
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
      object = JAXB.unmarshal(stream, type);
    }
    catch (IOException | DataBindingException ex)
    {
      throw new ContentTransformerException("could not unmarshall content", ex);
    } finally
    {
      IOUtil.close(stream);
    }

    return object;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true}, if the content type is compatible with 
   * application/xml.
   *
   *
   * @param type object type
   * @param contentType content type
   *
   * @return {@code true}, if the content type is compatible with 
   *   application/xml
   */
  @Override
  public boolean isResponsible(Class<?> type, String contentType)
  {
    return MediaType.valueOf(contentType).isCompatible(
      MediaType.APPLICATION_XML_TYPE);
  }
}
