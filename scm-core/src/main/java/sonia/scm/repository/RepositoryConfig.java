/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.repository;

import sonia.scm.Validateable;
import sonia.scm.config.Configuration;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Basic {@link Repository} configuration class.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement
public abstract class RepositoryConfig implements Validateable, Configuration {

  /** true if the plugin is disabled */
  private boolean disabled = false;
  /**
   * Returns true if the plugin is disabled.
   *
   *
   * @return true if the plugin is disabled
   * @since 1.13
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Returns true if the configuration object is valid.
   *
   *
   * @return true if the configuration object is valid
   */
  @Override
  public boolean isValid() {
    return true;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Enable or disable the plugin.
   *
   *
   * @param disabled
   * @since 1.13
   */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }



  /**
   * Specifies the identifier of the concrete {@link RepositoryConfig} when checking permissions of an object.
   * The permission Strings will have the following format: "configuration:*:ID", where the ID part is defined by this
   * method.
   *
   * For example: "configuration:read:git".
   *
   * No need to serialize this.
   *
   * @return identifier of this RepositoryConfig in permission strings
   */
  @Override
  @XmlTransient // Only for permission checks, don't serialize to XML
  public abstract String getId();
}
