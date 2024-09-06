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

package sonia.scm.store;

import com.google.common.collect.Maps;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.KeyGenerator;
import sonia.scm.xml.XmlStreams;
import sonia.scm.xml.XmlStreams.AutoCloseableXMLReader;
import sonia.scm.xml.XmlStreams.AutoCloseableXMLWriter;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static sonia.scm.store.CopyOnWrite.execute;

public class JAXBConfigurationEntryStore<V> implements ConfigurationEntryStore<V> {

  private static final String TAG_CONFIGURATION = "configuration";
  private static final String TAG_ENTRY = "entry";
  private static final String TAG_KEY = "key";
  private static final String TAG_VALUE = "value";

 
  private static final Logger LOG = LoggerFactory.getLogger(JAXBConfigurationEntryStore.class);


  private final File file;
  private final KeyGenerator keyGenerator;
  private final Class<V> type;
  private final TypedStoreContext<V> context;
  private final Map<String, V> entries = Maps.newHashMap();

  JAXBConfigurationEntryStore(File file, KeyGenerator keyGenerator, Class<V> type, TypedStoreContext<V> context) {
    this.file = file;
    this.keyGenerator = keyGenerator;
    this.type = type;
    this.context = context;
    // initial load
    execute(() -> {
      if (file.exists()) {
        load();
      }
    }).withLockedFileForRead(file);
  }

  @Override
  public void clear() {
    LOG.debug("clear configuration store");

    execute(() -> {
      entries.clear();
      store();
    }).withLockedFileForWrite(file);
  }

  @Override
  public String put(V item) {
    String id = keyGenerator.createKey();

    put(id, item);

    return id;
  }

  @Override
  public void put(String id, V item) {
    LOG.debug("put item {} to configuration store", id);

    execute(() -> {
      entries.put(id, item);
      store();
    }).withLockedFileForWrite(file);
  }

  @Override
  public void remove(String id) {
    LOG.debug("remove item {} from configuration store", id);

    execute(() -> {
      entries.remove(id);
      store();
    }).withLockedFileForWrite(file);
  }

  @Override
  public V get(String id) {
    LOG.trace("get item {} from configuration store", id);

    return entries.get(id);
  }

  @Override
  public Map<String, V> getAll() {
    LOG.trace("get all items from configuration store");

    return Collections.unmodifiableMap(entries);
  }

  private void load() {
    LOG.debug("load configuration from {}", file);
    execute(() ->
      context.withUnmarshaller(u -> {
        try (AutoCloseableXMLReader reader = XmlStreams.createReader(file)) {

          // configuration
          reader.nextTag();

          // entry start
          reader.nextTag();

          while (reader.isStartElement() && reader.getLocalName().equals(TAG_ENTRY)) {

            // read key
            reader.nextTag();

            String key = reader.getElementText();

            // read value
            reader.nextTag();

            JAXBElement<V> element = u.unmarshal(reader, type);

            if (!element.isNil()) {
              V v = element.getValue();

              LOG.trace("add element {} to configuration entry store", v);

              entries.put(key, v);
            } else {
              LOG.warn("could not unmarshall object of entry store");
            }

            // closed or new entry tag
            if (reader.nextTag() == END_ELEMENT) {

              // fixed format, start new entry
              reader.nextTag();
            }
          }
        }
      })).withLockedFileForRead(file);
  }

  private void store() {
    LOG.debug("store configuration to {}", file);

    context.withMarshaller(m -> {
      m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

      CopyOnWrite.withTemporaryFile(
        temp -> {
          try (AutoCloseableXMLWriter writer = XmlStreams.createWriter(temp)) {
            writer.writeStartDocument();

            // configuration start
            writer.writeStartElement(TAG_CONFIGURATION);
            writer.writeAttribute("type", "config-entry");

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
    });
  }
}
