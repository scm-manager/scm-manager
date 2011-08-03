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



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Sebastian Sdorra
 * @goal fix-descriptor
 * @phase process-resources
 */
public class FixDescriptorMojo extends AbstractMojo
{

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
    if (descriptor.exists() && descriptor.isFile())
    {
      Document document = createDocument(descriptor);
    }
    else
    {
      getLog().warn("no plugin descriptor found, skipping fix-descriptor goal");
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
   * @param document
   * @param parent
   * @param name
   * @param value
   */
  private void appendNode(Document document, Node parent, String name,
                          String value)
  {
    if (value != null)
    {
      Element node = document.createElement(name);

      node.setTextContent(value);
      parent.appendChild(node);
    }
  }

  /**
   * Method description
   *
   *
   * @param descriptor
   *
   * @return
   *
   * @throws MojoExecutionException
   */
  private Document createDocument(File descriptor) throws MojoExecutionException
  {
    Document document = null;

    try
    {
      document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
          descriptor);
      fixDescriptor(document);
      writeDocument(document);
    }
    catch (Exception ex)
    {
      throw new MojoExecutionException("could not parse plugin descriptor");
    }

    return document;
  }

  /**
   * Method description
   *
   *
   * @param document
   */
  private void fixDescriptor(Document document)
  {
    Element rootElement = document.getDocumentElement();
    NodeList informationNodeList =
      rootElement.getElementsByTagName("information");
    Node informationNode = null;

    for (int i = 0; i < informationNodeList.getLength(); i++)
    {
      Node node = informationNodeList.item(i);

      if ("information".equals(node.getNodeName()))
      {
        informationNode = node;

        break;
      }
    }

    if (informationNode == null)
    {
      informationNode = document.createElement("information");
      rootElement.appendChild(informationNode);
    }

    fixDescriptorInformations(document, informationNode);
  }

  /**
   * Method description
   *
   *
   * @param document
   * @param informationNode
   */
  private void fixDescriptorInformations(Document document,
          Node informationNode)
  {
    boolean groupId = false;
    boolean artifactId = false;
    boolean version = false;
    boolean name = false;
    boolean url = false;
    boolean description = false;
    boolean author = false;
    NodeList children = informationNode.getChildNodes();

    for (int i = 0; i < children.getLength(); i++)
    {
      Node node = children.item(i);
      String nodeName = node.getNodeName();

      if ("groupId".equals(nodeName))
      {
        groupId = true;
      }
      else if ("artifactId".equals(nodeName))
      {
        artifactId = true;
      }
      else if ("version".equals(nodeName))
      {
        version = true;
      }
      else if ("name".equals(nodeName))
      {
        name = true;
      }
      else if ("url".equals(nodeName))
      {
        url = true;
      }
      else if ("description".equals(nodeName))
      {
        description = true;
      }
      else if ("author".equals(nodeName))
      {
        author = true;
      }
    }

    if (!groupId)
    {
      appendNode(document, informationNode, "groupId", project.getGroupId());
    }

    if (!artifactId)
    {
      appendNode(document, informationNode, "artifactId",
                 project.getArtifactId());
    }

    if (!version)
    {
      appendNode(document, informationNode, "version", project.getVersion());
    }

    if (!name)
    {
      appendNode(document, informationNode, "name", project.getName());
    }

    if (!url)
    {
      appendNode(document, informationNode, "url", project.getUrl());
    }

    if (!description)
    {
      appendNode(document, informationNode, "description",
                 project.getDescription());
    }

    if (!author)
    {

      // ??
    }
  }

  /**
   * Method description
   *
   *
   * @param document
   *
   * @throws MojoExecutionException
   */
  private void writeDocument(Document document) throws MojoExecutionException
  {
    try
    {
      Transformer transformer =
        TransformerFactory.newInstance().newTransformer();

      transformer.transform(new DOMSource(document),
                            new StreamResult(descriptor));
    }
    catch (Exception ex)
    {
      throw new MojoExecutionException("could not write plugin descriptor");
    }
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
