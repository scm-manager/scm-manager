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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.KeyGenerator;
import sonia.scm.xml.IndentXMLStreamWriter;
import sonia.scm.xml.XmlStreams;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <V>
 */
public class JAXBConfigurationEntryStore<V> implements ConfigurationEntryStore<V>
{

  /** Field description */
  private static final String TAG_CONFIGURATION = "configuration";

  /** Field description */
  private static final String TAG_ENTRY = "entry";

  /** Field description */
  private static final String TAG_KEY = "key";

  /** Field description */
  private static final String TAG_VALUE = "value";

  /**
   * the logger for JAXBConfigurationEntryStore
   */
  private static final Logger logger =
    LoggerFactory.getLogger(JAXBConfigurationEntryStore.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param keyGenerator
   * @param file
   * @param type
   */
  JAXBConfigurationEntryStore(File file, KeyGenerator keyGenerator,
    Class<V> type)
  {
    this.file = file;
    this.keyGenerator = keyGenerator;
    this.type = type;

    try
    {
      this.context = JAXBContext.newInstance(type);

      if (file.exists())
      {
        load();
      }
    }
    catch (JAXBException ex)
    {
      throw new StoreException("could not create jaxb context", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void clear()
  {
    logger.debug("clear configuration store");

    synchronized (file)
    {
      entries.clear();
      store();
    }
  }

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  @Override
  public String put(V item)
  {
    String id = keyGenerator.createKey();

    put(id, item);

    return id;
  }

  /**
   * Method description
   *
   *
   * @param id
   * @param item
   */
  @Override
  public void put(String id, V item)
  {
    logger.debug("put item {} to configuration store", id);

    synchronized (file)
    {
      entries.put(id, item);
      store();
    }
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void remove(String id)
  {
    logger.debug("remove item {} from configuration store", id);

    synchronized (file)
    {
      entries.remove(id);
      store();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public V get(String id)
  {
    logger.trace("get item {} from configuration store", id);

    return entries.get(id);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Map<String, V> getAll()
  {
    logger.trace("get all items from configuration store");

    return Collections.unmodifiableMap(entries);
  }

  /**
   * Method description
   *
   *
   * @param predicate
   *
   * @return
   */
  @Override
  public Collection<V> getMatchingValues(Predicate<V> predicate)
  {
    return Collections2.filter(entries.values(), predicate);
  }

  //~--- methods --------------------------------------------------------------

  /**
   *   Method description
   *
   *
   *   @return
   */
  private void load()
  {
    logger.debug("load configuration from {}", file);

    XMLStreamReader reader = null;

    try
    {
      Unmarshaller u = context.createUnmarshaller();

      reader = XmlStreams.createReader(file);

      // configuration
      reader.nextTag();

      // entry start
      reader.nextTag();

      while (reader.isStartElement() && reader.getLocalName().equals(TAG_ENTRY))
      {

        // read key
        reader.nextTag();

        String key = reader.getElementText();

        // read value
        reader.nextTag();

        JAXBElement<V> element = u.unmarshal(reader, type);

        if (!element.isNil())
        {
          V v = element.getValue();

          logger.trace("add element {} to configuration entry store", v);

          entries.put(key, v);
        }
        else
        {
          logger.warn("could not unmarshall object of entry store");
        }

        // closed or new entry tag
        if (reader.nextTag() == XMLStreamReader.END_ELEMENT)
        {

          // fixed format, start new entry
          reader.nextTag();
        }
      }
    }
    catch (Exception ex)
    {
      throw new StoreException("could not load configuration", ex);
    }
    finally
    {
      XmlStreams.close(reader);
    }
  }

  /**
   * Method description
   *
   */
  private void store()
  {
    logger.debug("store configuration to {}", file);

    CopyOnWrite.withTemporaryFile(
      temp -> {
        try (IndentXMLStreamWriter writer = XmlStreams.createWriter(temp)) {
          writer.writeStartDocument();

          // configuration start
          writer.writeStartElement(TAG_CONFIGURATION);

          Marshaller m = context.createMarshaller();

          m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

          for (Entry<String, V> e : entries.entrySet()) {

            // entry start
            writer.writeStartElement(TAG_ENTRY);

            // key start
            writer.writeStartElement(TAG_KEY);
            writer.writeCharacters(e.getKey());

            // key end
            writer.writeEndElement();

            // value
            JAXBElement<V> je = new JAXBElement<>(QName.valueOf(TAG_VALUE), type,
              e.getValue());

            m.marshal(je, writer);

            // entry end
            writer.writeEndElement();
          }

          // configuration end
          writer.writeEndElement();
          writer.writeEndDocument();
        }
      },
      file.toPath()
    );
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final File file;

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private Map<String, V> entries = Maps.newHashMap();

  /** Field description */
  private KeyGenerator keyGenerator;

  /** Field description */
  private Class<V> type;
}
