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

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Plugin
{

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginInformation getInformation()
  {
    return information;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<String> getPackageSet()
  {
    return packageSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPath()
  {
    return path;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginResources getResources()
  {
    return resources;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param information
   */
  public void setInformation(PluginInformation information)
  {
    this.information = information;
  }

  /**
   * Method description
   *
   *
   * @param packageSet
   */
  public void setPackageSet(Set<String> packageSet)
  {
    this.packageSet = packageSet;
  }

  /**
   * Method description
   *
   *
   * @param path
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  /**
   * Method description
   *
   *
   * @param resources
   */
  public void setResources(PluginResources resources)
  {
    this.resources = resources;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PluginInformation information;

  /** Field description */
  @XmlElement(name = "package")
  @XmlElementWrapper(name = "packages")
  private Set<String> packageSet;

  /** Field description */
  @XmlTransient
  private String path;

  /** Field description */
  private PluginResources resources;
}
