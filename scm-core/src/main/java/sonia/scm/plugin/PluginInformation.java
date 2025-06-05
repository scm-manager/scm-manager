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

package sonia.scm.plugin;

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import sonia.scm.Validateable;
import sonia.scm.util.Util;

import java.io.Serializable;

@Data
@StaticPermissions(
  value = "plugin",
  generatedClass = "PluginPermissions",
  permissions = {},
  globalPermissions = {"read", "write"},
  custom = true, customGlobal = true
)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "plugin-information")
public class PluginInformation implements PermissionObject, Validateable, Cloneable, Serializable {

  private static final long serialVersionUID = 461382048865977206L;

  private String name;
  private String version;
  private String displayName;
  private String description;
  private String author;
  private String category;
  private String avatarUrl;

  @Override
  public PluginInformation clone() {
    PluginInformation clone = new PluginInformation();
    clone.setName(name);
    clone.setVersion(version);
    clone.setDisplayName(displayName);
    clone.setDescription(description);
    clone.setAuthor(author);
    clone.setCategory(category);
    clone.setAvatarUrl(avatarUrl);
    return clone;
  }

  @Override
  public String getId() {
    return getName(true);
  }

  public String getName(boolean withVersion) {
    StringBuilder id = new StringBuilder(name);

    if (withVersion) {
      id.append(":").append(version);
    }
    return id.toString();
  }

  @Override
  public boolean isValid() {
    return Util.isNotEmpty(name) && Util.isNotEmpty(version);
  }
}
