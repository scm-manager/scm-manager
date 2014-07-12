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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
