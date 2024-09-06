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

package sonia.scm.util;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Util methods to handle xml files.
 *
 * @since 2.0.0
 */
public final class XmlUtil
{

  private XmlUtil() {}


  public static Document createDocument(InputStream stream)
    throws ParserConfigurationException, SAXException, IOException
  {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
      stream);
  }

  public static Multimap<String, String> values(InputStream input,
    String... entries)
    throws IOException
  {
    Multimap<String, String> values = HashMultimap.create();

    if ((entries != null) && (entries.length > 0))
    {
      try
      {
        Document doc = createDocument(input);

        values(values, doc, entries);
      }
      catch (DOMException | ParserConfigurationException | SAXException ex)
      {
        throw new IOException("could not parse document", ex);
      }
    }

    return values;
  }

  public static Multimap<String, String> values(Document doc, String... entries)
    throws IOException
  {
    Multimap<String, String> values = HashMultimap.create();

    if ((entries != null) && (entries.length > 0))
    {
      values(values, doc, entries);
    }

    return values;
  }

  private static void values(Multimap<String, String> values, Document doc,
    String... entries)
    throws IOException
  {
    for (String entry : entries)
    {
      NodeList list = doc.getElementsByTagName(entry);

      for (int i = 0; i < list.getLength(); i++)
      {
        Node node = list.item(i);
        String value = node.getTextContent();

        if (value != null)
        {
          values.put(entry, value);
        }
      }
    }
  }
}
