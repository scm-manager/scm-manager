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



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Sebastian Sdorra
 * @goal append-dependencies
 * @phase process-classes
 * @requiresDependencyResolution compile+runtime
 */
public class AppendDependenciesMojo extends AbstractMojo
{

  /** Field description */
  private static final String ELEMENT_ARTIFACTID = "artifactId";

  /** Field description */
  private static final String ELEMENT_DEPENDENCIES = "dependencies";

  /** Field description */
  private static final String ELEMENT_DEPENDENCY = "dependency";

  /** Field description */
  private static final String ELEMENT_GROUPID = "groupId";

  /** Field description */
  private static final String ELEMENT_INFORMATION = "information";

  /** Field description */
  private static final String PLUGIN_DESCRIPTOR = "META-INF/scm/plugin.xml";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  public void doExecute() throws MojoExecutionException, MojoFailureException
  {
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

    try
    {
      ClassLoader classLoader = ClassLoaders.createRuntimeClassLoader(project);

      Thread.currentThread().setContextClassLoader(classLoader);
      appendDependencies(classLoader);
    }
    catch (ParserConfigurationException ex)
    {
      throw new MojoExecutionException("could not parse plugin descriptor", ex);
    }
    catch (SAXException ex)
    {
      throw new MojoExecutionException("could not parse plugin descriptor", ex);
    }
    catch (DependencyResolutionRequiredException ex)
    {
      throw new MojoExecutionException("could not setup classloader", ex);
    }
    catch (IOException ex)
    {
      throw new MojoExecutionException("could not setup classloader", ex);
    }
    catch (TransformerException ex)
    {
      throw new MojoExecutionException("could not write plugin.xml", ex);
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    if (descriptor.exists())
    {
      doExecute();
    }
    else
    {
      getLog().warn(
        "no plugin descriptor found, skipping append-dependencies goal");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public File getDescriptor()
  {
    return descriptor;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public MavenProject getProject()
  {
    return project;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param descriptor
   */
  public void setDescriptor(File descriptor)
  {
    this.descriptor = descriptor;
  }

  /**
   * Method description
   *
   *
   * @param project
   */
  public void setProject(MavenProject project)
  {
    this.project = project;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classLoader
   *
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws TransformerException
   */
  private void appendDependencies(ClassLoader classLoader)
    throws IOException, ParserConfigurationException, SAXException,
    TransformerException
  {
    Set<String> dependencies = new HashSet<String>();
    Enumeration<URL> descriptors = classLoader.getResources(PLUGIN_DESCRIPTOR);

    while (descriptors.hasMoreElements())
    {
      URL url = descriptors.nextElement();

      appendDependency(dependencies, url);
    }

    if (dependencies.isEmpty())
    {
      getLog().info("no plugin dependencies found");
    }
    else
    {
      getLog().info("update plugin descriptor");
      rewritePluginDescriptor(dependencies);
    }
  }

  /**
   * Method description
   *
   *
   * @param dependencies
   * @param url
   *
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  private void appendDependency(Set<String> dependencies, URL url)
    throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilder builder =
      DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputStream is = null;

    try
    {
      is = url.openStream();

      Document doc = builder.parse(is);

      String pluginid = parseDescriptor(doc);

      if (!pluginid.equals(
        project.getGroupId().concat(":").concat(project.getArtifactId())))
      {
        dependencies.add(pluginid);
      }

    }
    finally
    {
      if (is != null)
      {
        is.close();
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param doc
   *
   * @return
   */
  private String parseDescriptor(Document doc)
  {
    String id = null;
    NodeList nodeList = doc.getElementsByTagName(ELEMENT_INFORMATION);

    for (int i = 0; i < nodeList.getLength(); i++)
    {
      Node node = nodeList.item(i);

      if (isElement(node, ELEMENT_INFORMATION))
      {
        id = parseInformationNode(node);

        break;
      }
    }

    return id;
  }

  /**
   * Method description
   *
   *
   * @param informationNode
   *
   * @return
   */
  private String parseInformationNode(Node informationNode)
  {
    String groupid = null;
    String artifactid = null;
    NodeList nodeList = informationNode.getChildNodes();

    for (int i = 0; i < nodeList.getLength(); i++)
    {
      Node node = nodeList.item(i);

      if (isElement(node, ELEMENT_GROUPID))
      {
        groupid = node.getTextContent();
      }
      else if (isElement(node, ELEMENT_ARTIFACTID))
      {
        artifactid = node.getTextContent();
      }
    }

    if ((groupid == null) || (artifactid == null))
    {
      throw new RuntimeException(
        "descriptor does not contain groupid or artifactid");
    }

    return groupid.concat(":").concat(artifactid);
  }

  /**
   * Method description
   *
   *
   * @param dependencies
   *
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  private void rewritePluginDescriptor(Set<String> dependencies)
    throws SAXException, IOException, ParserConfigurationException,
    TransformerConfigurationException, TransformerException
  {
    DocumentBuilder builder =
      DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(descriptor);
    Element dependenciesEl = doc.createElement(ELEMENT_DEPENDENCIES);

    doc.getDocumentElement().appendChild(dependenciesEl);

    for (String pluginid : dependencies)
    {
      Element dependencyEl = doc.createElement(ELEMENT_DEPENDENCY);

      dependencyEl.setTextContent(pluginid);
      dependenciesEl.appendChild(dependencyEl);
    }

    Transformer transformer = TransformerFactory.newInstance().newTransformer();

    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(new DOMSource(doc), new StreamResult(descriptor));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param node
   * @param name
   *
   * @return
   */
  private boolean isElement(Node node, String name)
  {
    return (node.getNodeType() == Node.ELEMENT_NODE)
      && name.equals(node.getNodeName());
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * @parameter default-value="${project.build.directory}/classes/META-INF/scm/plugin.xml"
   */
  private File descriptor;

  /**
   * The maven project in question.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;
}
