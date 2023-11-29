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
  private PluginType type = PluginType.SCM;

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
    clone.setType(type);
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

  public enum PluginType {
    SCM,
    CLOUDOGU
  }
}
