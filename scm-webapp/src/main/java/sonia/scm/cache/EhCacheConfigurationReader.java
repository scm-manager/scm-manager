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



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sonia.scm.SCMContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Sebastian Sdorra
 */
public class EhCacheConfigurationReader
{

  /** Field description */
  private static final String ATTRIBUTE_NAME = "name";

  /** Field description */
  private static final String DEFAULT = "/config/ehcache.xml";

  /** Field description */
  private static final String MANUAL_RESOURCE =
    "ext".concat(File.separator).concat("cache.xml");

  /** Field description */
  private static final String MODULE_RESOURCES = "/META-INF/scm/cache.xml";

  /**
   * the logger for EhCacheConfigurationReader
   */
  private static final Logger logger =
    LoggerFactory.getLogger(EhCacheConfigurationReader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public EhCacheConfigurationReader()
  {
    try
    {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (ParserConfigurationException ex)
    {
      throw new RuntimeException("could not create document builder", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public InputStream read()
  {
    URL defaultConfig = getDefaultResource();

    if (defaultConfig == null)
    {
      throw new IllegalStateException(
        "could not find default cache configuration");
    }

    readConfiguration(defaultConfig, true);

    Iterator<URL> it = getModuleResources();

    while (it.hasNext())
    {
      readConfiguration(it.next(), false);
    }

    File manualFile = getManualFileResource();

    if (manualFile.exists())
    {
      try
      {
        readConfiguration(manualFile.toURI().toURL(), false);
      }
      catch (MalformedURLException ex)
      {
        logger.error("malformed url", ex);
      }
    }
    else
    {
      logger.warn("could not find manual configuration at {}", manualFile);
    }

    Document doc = createMergedConfiguration();

    return createInputStream(doc);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  @VisibleForTesting
  protected URL getDefaultResource()
  {
    return EhCacheConfigurationReader.class.getResource(DEFAULT);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @VisibleForTesting
  protected File getManualFileResource()
  {
    return new File(SCMContext.getContext().getBaseDirectory(),
      MANUAL_RESOURCE);
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  @VisibleForTesting
  protected Iterator<URL> getModuleResources()
  {
    Iterator<URL> it = null;

    try
    {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      if (classLoader == null)
      {
        classLoader = EhCacheConfigurationReader.class.getClassLoader();
      }

      Enumeration<URL> enm = classLoader.getResources(MODULE_RESOURCES);

      if (enm != null)
      {
        it = Iterators.forEnumeration(enm);
      }

    }
    catch (IOException ex)
    {
      logger.error("could not read cache module resources", ex);
    }

    if (it == null)
    {
      it = Iterators.emptyIterator();
    }

    return it;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param doc
   *
   * @return
   */
  private InputStream createInputStream(Document doc)
  {
    InputStream stream;
    Transformer transformer;

    try
    {
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      transformer.transform(new DOMSource(doc), new StreamResult(baos));

      if (logger.isTraceEnabled())
      {
        logger.trace("effective ehcache configuration: {}", baos.toString());
      }

      stream = new ByteArrayInputStream(baos.toByteArray());
    }
    catch (Exception ex)
    {
      throw new IllegalStateException("could not create transformer", ex);
    }

    return stream;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Document createMergedConfiguration()
  {
    Document merged = builder.newDocument();

    Element rootEl = merged.createElementNS("http://ehcache.org/ehcache.xsd",
                       "ehcache");

    for (Node node : map.values())
    {
      Node mergedNode = merged.adoptNode(node);

      rootEl.appendChild(mergedNode);
    }

    merged.appendChild(rootEl);

    return merged;
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param throwException
   */
  private void readConfiguration(URL url, boolean throwException)
  {
    logger.debug("read cache configuration from url {}", url.toExternalForm());

    InputStream stream = null;

    try
    {
      stream = url.openStream();

      Document document = builder.parse(stream);
      Element rootEl = document.getDocumentElement();

      readConfiguration(rootEl);
    }
    catch (Exception ex)
    {
      if (throwException)
      {
        throw new RuntimeException(
          "could not read configuration at ".concat(url.toExternalForm()), ex);
      }
      else
      {
        logger.warn("could not read configuration at {}", url.toExternalForm());
      }
    }
    finally
    {
      Closeables.closeQuietly(stream);
    }
  }

  /**
   * Method description
   *
   *
   * @param rootEl
   */
  private void readConfiguration(Element rootEl)
  {
    NodeList list = rootEl.getChildNodes();

    for (int i = 0; i < list.getLength(); i++)
    {
      Node node = list.item(i);

      if (Node.ELEMENT_NODE == node.getNodeType())
      {
        String element = node.getNodeName();
        String name = null;
        Node nameNode = node.getAttributes().getNamedItem(ATTRIBUTE_NAME);

        if (nameNode != null)
        {
          name = nameNode.getNodeValue();
        }

        map.put(new Id(element, name), node);
      }
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/03/19
   * @author         Enter your name here...
   */
  private static class Id
  {

    /**
     * Constructs ...
     *
     *
     * @param element
     * @param name
     */
    public Id(String element, String name)
    {
      this.element = element;
      this.name = name;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }

      if (getClass() != obj.getClass())
      {
        return false;
      }

      final Id other = (Id) obj;

      return Objects.equal(element, other.element)
        && Objects.equal(name, other.name);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode()
    {
      return Objects.hashCode(element, name);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString()
    {

      //J-
      return Objects.toStringHelper(this)
                    .add("element", element)
                    .add("name", name)
                    .toString();
      //J+
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String element;

    /** Field description */
    private String name;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private DocumentBuilder builder;

  /** Field description */
  private Map<Id, Node> map = Maps.newLinkedHashMap();
}
