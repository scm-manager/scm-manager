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

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class PluginCenterDto implements Serializable {

  @XmlElement(name = "_embedded")
  private Embedded embedded;

  public Embedded getEmbedded() {
    return embedded;
  }

  @XmlRootElement(name = "_embedded")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Embedded {

    @XmlElement(name = "plugins")
    private List<Plugin> plugins;

    public List<Plugin> getPlugins() {
      if (plugins == null) {
        plugins = ImmutableList.of();
      }
      return plugins;
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "plugins")
  @Getter
  @AllArgsConstructor
  public static class Plugin {

    private String name;
    private String version;
    private String displayName;
    private String description;
    private String category;
    private String author;
    private String avatarUrl;
    private String sha256sum;

    @XmlElement(name = "conditions")
    private Condition conditions;

    @XmlElement(name = "dependencies")
    private Set<String> dependencies;

    @XmlElement(name = "optionalDependencies")
    private Set<String> optionalDependencies;

    @XmlElement(name = "_links")
    private Map<String, Link> links;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "conditions")
  @Getter
  @AllArgsConstructor
  public static class Condition {

    private List<String> os;
    private String arch;
    private String minVersion;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  static class Link {
    private String href;
  }
}
