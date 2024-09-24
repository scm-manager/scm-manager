/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
