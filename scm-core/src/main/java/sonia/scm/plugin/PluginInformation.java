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

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

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
public class PluginInformation implements Validateable
{

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

    if ((this.artifactId == null)
        ? (other.artifactId != null)
        : !this.artifactId.equals(other.artifactId))
    {
      return false;
    }

    if ((this.author == null)
        ? (other.author != null)
        : !this.author.equals(other.author))
    {
      return false;
    }

    if ((this.category == null)
        ? (other.category != null)
        : !this.category.equals(other.category))
    {
      return false;
    }

    if ((this.condition != other.condition)
        && ((this.condition == null) ||!this.condition.equals(other.condition)))
    {
      return false;
    }

    if ((this.description == null)
        ? (other.description != null)
        : !this.description.equals(other.description))
    {
      return false;
    }

    if ((this.groupId == null)
        ? (other.groupId != null)
        : !this.groupId.equals(other.groupId))
    {
      return false;
    }

    if ((this.name == null)
        ? (other.name != null)
        : !this.name.equals(other.name))
    {
      return false;
    }

    if ((this.screenshots != other.screenshots)
        && ((this.screenshots == null)
            ||!this.screenshots.equals(other.screenshots)))
    {
      return false;
    }

    if (this.state != other.state)
    {
      return false;
    }

    if ((this.url == null)
        ? (other.url != null)
        : !this.url.equals(other.url))
    {
      return false;
    }

    if ((this.version == null)
        ? (other.version != null)
        : !this.version.equals(other.version))
    {
      return false;
    }

    if ((this.wiki == null)
        ? (other.wiki != null)
        : !this.wiki.equals(other.wiki))
    {
      return false;
    }

    return true;
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
    int hash = 5;

    hash = 79 * hash + ((this.artifactId != null)
                        ? this.artifactId.hashCode()
                        : 0);
    hash = 79 * hash + ((this.author != null)
                        ? this.author.hashCode()
                        : 0);
    hash = 79 * hash + ((this.category != null)
                        ? this.category.hashCode()
                        : 0);
    hash = 79 * hash + ((this.condition != null)
                        ? this.condition.hashCode()
                        : 0);
    hash = 79 * hash + ((this.description != null)
                        ? this.description.hashCode()
                        : 0);
    hash = 79 * hash + ((this.groupId != null)
                        ? this.groupId.hashCode()
                        : 0);
    hash = 79 * hash + ((this.name != null)
                        ? this.name.hashCode()
                        : 0);
    hash = 79 * hash + ((this.screenshots != null)
                        ? this.screenshots.hashCode()
                        : 0);
    hash = 79 * hash + ((this.state != null)
                        ? this.state.hashCode()
                        : 0);
    hash = 79 * hash + ((this.url != null)
                        ? this.url.hashCode()
                        : 0);
    hash = 79 * hash + ((this.version != null)
                        ? this.version.hashCode()
                        : 0);
    hash = 79 * hash + ((this.wiki != null)
                        ? this.wiki.hashCode()
                        : 0);

    return hash;
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
    StringBuilder id = new StringBuilder(groupId);

    id.append(":").append(artifactId).append(":");

    return id.append(version).toString();
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
  private String url;

  /** Field description */
  private String version;

  /** Field description */
  private String wiki;
}
