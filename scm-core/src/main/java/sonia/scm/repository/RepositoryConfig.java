/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import sonia.scm.Validateable;
import sonia.scm.config.Configuration;

/**
 * Basic {@link Repository} configuration class.
 *
 */
@XmlRootElement
public abstract class RepositoryConfig implements Validateable, Configuration {

  private boolean disabled = false;
  /**
   * Returns true if the plugin is disabled.
   *
   *  @since 1.13
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Returns true if the configuration object is valid.
   */
  @Override
  public boolean isValid() {
    return true;
  }


  /**
   * Enable or disable the plugin.
   *
   *  @since 1.13
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
