/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.KeyGenerator;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <V>
 */
public class JAXBConfigurationEntryStore<V>
  implements ConfigurationEntryStore<V>
{

  /** Field description */
  private static final Object LOCK = new Object();

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
  JAXBConfigurationEntryStore(KeyGenerator keyGenerator, File file,
    Class<V> type)
  {
    this.keyGenerator = keyGenerator;
    this.file = file;
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

    synchronized (LOCK)
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

    synchronized (LOCK)
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

    synchronized (LOCK)
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
   * Method description
   *
   *
   * @param writer
   */
  private void close(XMLStreamWriter writer)
  {
    if (writer != null)
    {
      try
      {
        writer.close();
      }
      catch (XMLStreamException ex)
      {
        logger.error("could not close writer", ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param reader
   */
  private void close(XMLStreamReader reader)
  {
    if (reader != null)
    {
      try
      {
        reader.close();
      }
      catch (XMLStreamException ex)
      {
        logger.error("could not close reader", ex);
      }
    }
  }

  /**
   *   Method description
   *
   *
   *   @return
   */
  private void load()
  {
    logger.debug("load configuration from {}", file);

    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    XMLStreamReader reader = null;

    try
    {
      Unmarshaller u = context.createUnmarshaller();

      reader = xmlInputFactory.createXMLStreamReader(new StreamSource(file));
      reader.nextTag();
      reader.nextTag();

      while (reader.isStartElement() && reader.getLocalName().equals(TAG_ENTRY))
      {

        // read key
        reader.nextTag();

        String key = reader.getElementText();

        // read entry
        reader.nextTag();

        V v = (V) u.unmarshal(reader, type).getValue();

        entries.put(key, v);
      }
    }
    catch (Exception ex)
    {
      throw new StoreException("could not load configuration", ex);
    }
    finally
    {
      close(reader);
    }
  }

  /**
   * Method description
   *
   */
  private void store()
  {
    logger.debug("store configuration to {}", file);

    XMLStreamWriter writer = null;

    try
    {
      writer = XMLOutputFactory.newFactory().createXMLStreamWriter(
        new FileOutputStream(file));
      writer.writeStartDocument();
      writer.writeStartElement(TAG_CONFIGURATION);

      Marshaller m = context.createMarshaller();

      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

      for (Entry<String, V> e : entries.entrySet())
      {
        writer.writeStartElement(TAG_ENTRY);
        writer.writeStartElement(TAG_KEY);
        writer.writeCharacters(e.getKey());
        writer.writeEndElement();

        JAXBElement<V> je = new JAXBElement<V>(QName.valueOf(TAG_VALUE), type,
                              e.getValue());

        m.marshal(je, writer);
      }

      writer.writeEndElement();
      writer.writeEndDocument();
    }
    catch (Exception ex)
    {
      throw new StoreException("could not store configuration", ex);
    }
    finally
    {
      close(writer);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private Map<String, V> entries = Maps.newHashMap();

  /** Field description */
  private File file;

  /** Field description */
  private KeyGenerator keyGenerator;

  /** Field description */
  private Class<V> type;
}
