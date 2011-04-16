/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgChangesetParser
{

  /** the logger for HgChangesetParser */
  private static final Logger logger =
    LoggerFactory.getLogger(HgChangesetParser.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param in
   *
   * @return
   *
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  public List<Changeset> parse(InputSource in)
          throws SAXException, IOException, ParserConfigurationException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();

    return parse(builder.parse(in));
  }

  /**
   * Method description
   *
   *
   * @param document
   *
   * @return
   */
  private List<Changeset> parse(Document document)
  {
    List<Changeset> changesetList = new ArrayList<Changeset>();
    NodeList changesetNodeList = document.getElementsByTagName("changeset");

    if (changesetNodeList != null)
    {
      for (int i = 0; i < changesetNodeList.getLength(); i++)
      {
        Node changesetNode = changesetNodeList.item(i);
        Changeset changeset = parseChangesetNode(changesetNode);

        if ((changeset != null) && changeset.isValid())
        {
          changesetList.add(changeset);
        }
      }
    }

    return changesetList;
  }

  /**
   * Method description
   *
   *
   * @param changeset
   * @param node
   */
  private void parseChangesetChildNode(Changeset changeset, Node node)
  {
    String name = node.getNodeName();
    String value = node.getTextContent();

    if (Util.isNotEmpty(value))
    {
      if ("id".equals(name))
      {
        changeset.setId(value);
      }
      else if ("author".equals(name))
      {
        changeset.setAuthor(value);
      }
      else if ("description".equals(name))
      {
        changeset.setDescription(value);
      }
      else if ("date".equals(name))
      {
        try
        {
          Date date = dateFormat.parse(value);

          changeset.setDate(date.getTime());
        }
        catch (ParseException ex)
        {
          logger.warn("could not parse date", ex);
        }
      }
      else if ("tags".equals(name))
      {
        changeset.setTags(getList(value));
      }
      else if ("branches".equals(name))
      {
        changeset.setBranches(getList(value));
      }
      else if ("files-added".equals(name))
      {
        getModifications(changeset).setAdded(getList(value));
      }
      else if ("files-mods".equals(name))
      {
        getModifications(changeset).setModified(getList(value));
      }
      else if ("files-dels".equals(name))
      {
        getModifications(changeset).setRemoved(getList(value));
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param changesetNode
   *
   * @return
   */
  private Changeset parseChangesetNode(Node changesetNode)
  {
    Changeset changeset = new Changeset();
    NodeList childrenNodeList = changesetNode.getChildNodes();

    if (childrenNodeList != null)
    {
      for (int i = 0; i < childrenNodeList.getLength(); i++)
      {
        Node child = childrenNodeList.item(i);

        parseChangesetChildNode(changeset, child);
      }
    }

    return changeset;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  private List<String> getList(String value)
  {
    return Arrays.asList(value.split(" "));
  }

  /**
   * Method description
   *
   *
   * @param changeset
   *
   * @return
   */
  private Modifications getModifications(Changeset changeset)
  {
    Modifications mods = changeset.getModifications();

    if (mods == null)
    {
      mods = new Modifications();
      changeset.setModifications(mods);
    }

    return mods;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SimpleDateFormat dateFormat =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
}
