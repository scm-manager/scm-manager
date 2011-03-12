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

import sonia.scm.xml.XmlIntervalAdapter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "configuration")
public class BackendConfiguration
{

  /**
   * Method description
   *
   *
   * @return
   */
  public List<File> getDirectories()
  {
    return directories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<PluginRepository> getRepositories()
  {
    return repositories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public long getScannInterval()
  {
    return scannInterval;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isMultithreaded()
  {
    return multithreaded;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param directories
   */
  public void setDirectories(List<File> directories)
  {
    this.directories = directories;
  }

  /**
   * Method description
   *
   *
   * @param multithreaded
   */
  public void setMultithreaded(boolean multithreaded)
  {
    this.multithreaded = multithreaded;
  }

  /**
   * Method description
   *
   *
   * @param repositories
   */
  public void setRepositories(List<PluginRepository> repositories)
  {
    this.repositories = repositories;
  }

  /**
   * Method description
   *
   *
   * @param scannInterval
   */
  public void setScannInterval(long scannInterval)
  {
    this.scannInterval = scannInterval;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "directory")
  @XmlElementWrapper(name = "directories")
  private List<File> directories;

  /** Field description */
  private boolean multithreaded = true;

  /** Field description */
  @XmlElement(name = "repository")
  @XmlElementWrapper(name = "repositories")
  private List<PluginRepository> repositories;

  /** Field description */
  @XmlElement(name = "scann-interval")
  @XmlJavaTypeAdapter(XmlIntervalAdapter.class)
  private Long scannInterval;
}
