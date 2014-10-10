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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "plugin-information")
public class PluginInformation implements Validateable, Cloneable, Serializable
{

  /** Field description */
  private static final long serialVersionUID = 461382048865977206L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @since 1.11
   */
  @Override
  public PluginInformation clone()
  {
    PluginInformation clone = new PluginInformation();

    clone.setArtifactId(artifactId);
    clone.setAuthor(author);
    clone.setCategory(category);
    clone.setTags(tags);

    if (condition != null)
    {
      clone.setCondition(condition.clone());
    }

    clone.setDescription(description);
    clone.setGroupId(groupId);
    clone.setName(name);

    if (Util.isNotEmpty(screenshots))
    {
      clone.setScreenshots(new ArrayList<String>(screenshots));
    }

    clone.setState(state);
    clone.setUrl(url);
    clone.setVersion(version);
    clone.setWiki(wiki);

    return clone;
  }

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

    final PluginInformation other = (PluginInformation) obj;

    //J-
    return Objects.equal(artifactId, other.artifactId)
      && Objects.equal(author, other.author)
      && Objects.equal(category, other.category)
      && Objects.equal(tags, other.tags)
      && Objects.equal(condition, other.condition)
      && Objects.equal(description, other.description)
      && Objects.equal(groupId, other.groupId)
      && Objects.equal(name, other.name)
      && Objects.equal(screenshots, other.screenshots)
      && Objects.equal(state, other.state) 
      && Objects.equal(url, other.url)
      && Objects.equal(version, other.version)
      && Objects.equal(wiki, other.wiki);
    //J+
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
    return Objects.hashCode(artifactId, author, category, tags, condition,
      description, groupId, name, screenshots, state, url, version, wiki);
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
                  .add("artifactId", artifactId)
                  .add("author", author)
                  .add("category", category)
                  .add("tags", tags)
                  .add("condition", condition)
                  .add("description", description)
                  .add("groupId", groupId)
                  .add("name", name)
                  .add("screenshots", screenshots)
                  .add("state", state)
                  .add("url", url)
                  .add("version", version)
                  .add("wiki", wiki)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getArtifactId()
  {
    return artifactId;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAuthor()
  {
    return author;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getCategory()
  {
    return category;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginCondition getCondition()
  {
    return condition;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getGroupId()
  {
    return groupId;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return getId(true);
  }

  /**
   * Method description
   *
   *
   * @param withVersion
   *
   * @return
   * @since 1.21
   */
  public String getId(boolean withVersion)
  {
    StringBuilder id = new StringBuilder(groupId);

    id.append(":").append(artifactId);

    if (withVersion)
    {
      id.append(":").append(version);
    }

    return id.toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getScreenshots()
  {
    return screenshots;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginState getState()
  {
    return state;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getTags()
  {
    return tags;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getWiki()
  {
    return wiki;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(groupId) && Util.isNotEmpty(artifactId)
      && Util.isNotEmpty(name) && Util.isNotEmpty(version);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param artifactId
   */
  public void setArtifactId(String artifactId)
  {
    this.artifactId = artifactId;
  }

  /**
   * Method description
   *
   *
   * @param author
   */
  public void setAuthor(String author)
  {
    this.author = author;
  }

  /**
   * Method description
   *
   *
   * @param category
   */
  public void setCategory(String category)
  {
    this.category = category;
  }

  /**
   * Method description
   *
   *
   * @param condition
   */
  public void setCondition(PluginCondition condition)
  {
    this.condition = condition;
  }

  /**
   * Method description
   *
   *
   * @param description
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Method description
   *
   *
   * @param groupId
   */
  public void setGroupId(String groupId)
  {
    this.groupId = groupId;
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Method description
   *
   *
   * @param screenshots
   */
  public void setScreenshots(List<String> screenshots)
  {
    this.screenshots = screenshots;
  }

  /**
   * Method description
   *
   *
   * @param state
   */
  public void setState(PluginState state)
  {
    this.state = state;
  }

  /**
   * Method description
   *
   *
   * @param tags
   */
  public void setTags(List<String> tags)
  {
    this.tags = tags;
  }

  /**
   * Method description
   *
   *
   * @param url
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  /**
   * Method description
   *
   *
   * @param version
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  /**
   * Method description
   *
   *
   * @param wiki
   */
  public void setWiki(String wiki)
  {
    this.wiki = wiki;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String artifactId;

  /** Field description */
  private String author;

  /** Field description */
  private String category;

  /** Field description */
  private PluginCondition condition;

  /** Field description */
  private String description;

  /** Field description */
  private String groupId;

  /** Field description */
  private String name;

  /** Field description */
  @XmlElement(name = "screenshot")
  @XmlElementWrapper(name = "screenshots")
  private List<String> screenshots;

  /** Field description */
  private PluginState state;

  /** Field description */
  @XmlElement(name = "tag")
  @XmlElementWrapper(name = "tags")
  private List<String> tags;

  /** Field description */
  private String url;

  /** Field description */
  private String version;

  /** Field description */
  private String wiki;
}
