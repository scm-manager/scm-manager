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

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.Validateable;
import sonia.scm.util.Util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@StaticPermissions(
  value = "plugin", 
  generatedClass = "PluginPermissions", 
  permissions = {},
  globalPermissions = { "read", "manage" },
  custom = true, customGlobal = true
)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "plugin-information")
public class PluginInformation 
       implements PermissionObject, Validateable, Cloneable, Serializable
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
    clone.setName(name);
    clone.setAuthor(author);
    clone.setCategory(category);
    clone.setDescription(description);
    clone.setState(state);
    clone.setVersion(version);

    if (condition != null)
    {
      clone.setCondition(condition.clone());
    }

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
    return
      Objects.equal(author, other.author)
      && Objects.equal(category, other.category)
      && Objects.equal(condition, other.condition)
      && Objects.equal(description, other.description)
      && Objects.equal(name, other.name)
      && Objects.equal(state, other.state)
      && Objects.equal(version, other.version);
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
    return Objects.hashCode(author, category, condition,
      description, name, state, version);
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
    return MoreObjects.toStringHelper(this)
                  .add("author", author)
                  .add("category", category)
                  .add("condition", condition)
                  .add("description", description)
                  .add("name", name)
                  .add("state", state)
                  .add("version", version)
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
  @Override
  public String getId()
  {
    return getName(true);
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
  public String getName(boolean withVersion)
  {
    StringBuilder id = new StringBuilder(name);

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
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(name) && Util.isNotEmpty(version);
  }

  //~--- set methods ----------------------------------------------------------

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
   * @param version
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String author;

  /** Field description */
  private String category;

  /** Field description */
  private PluginCondition condition;

  /** Field description */
  private String description;

  /** Field description */
  private String name;

  /** Field description */
  private PluginState state;

  /** Field description */
  private String version;

}
