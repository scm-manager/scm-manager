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
    
package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

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
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class XmlUtil
{

  /**
   * Constructs ...
   *
   */
  private XmlUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Create {@link Document} from {@link InputStream}.
   *
   *
   * @param stream input stream
   *
   * @return generated document
   *
   *
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  public static Document createDocument(InputStream stream)
    throws ParserConfigurationException, SAXException, IOException
  {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
      stream);
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param entries
   *
   * @return
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param doc
   * @param entries
   *
   * @return
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param values
   * @param doc
   * @param entries
   *
   * @throws IOException
   */
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
