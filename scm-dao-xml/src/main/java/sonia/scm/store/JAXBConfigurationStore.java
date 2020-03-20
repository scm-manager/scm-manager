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
    
package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * JAXB implementation of {@link ConfigurationStore}.
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public class JAXBConfigurationStore<T> extends AbstractStore<T> {

  /**
   * the logger for JAXBConfigurationStore
   */
  private static final Logger LOG = LoggerFactory.getLogger(JAXBConfigurationStore.class);

  private Class<T> type;
  
  private File configFile;
  
  private JAXBContext context;
 
  public JAXBConfigurationStore(Class<T> type, File configFile) {
    this.type = type;

    try {
      context = JAXBContext.newInstance(type);
      this.configFile = configFile;
    }
    catch (JAXBException ex) {
      throw new StoreException("failed to create jaxb context", ex);
    }
  }

  /**
   * Returns type of stored object.
   *
   *
   * @return type
   */
  public Class<T> getType() {
    return type;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T readObject() {
    LOG.debug("load {} from store {}", type, configFile);

    T result = null;

    if (configFile.exists()) {
      try {
        result = (T) context.createUnmarshaller().unmarshal(configFile);
      }
      catch (JAXBException ex) {
        throw new StoreException("failed to unmarshall object", ex);
      }
    }

    return result;
  }

  @Override
  protected void writeObject(T object) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("store {} to {}", object.getClass().getName(),
        configFile.getPath());
    }

    try {
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      CopyOnWrite.withTemporaryFile(
        temp -> marshaller.marshal(object, temp.toFile()),
        configFile.toPath()
      );
    }
    catch (JAXBException ex) {
      throw new StoreException("failed to marshall object", ex);
    }
  }
}
