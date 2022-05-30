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

    @XmlElement(name = "plugin-sets")
    private List<PluginSet> pluginSets;

    public List<Plugin> getPlugins() {
      if (plugins == null) {
        plugins = List.of();
      }
      return plugins;
    }

    public List<PluginSet> getPluginSets() {
      if (pluginSets == null) {
        pluginSets = List.of();
      }
      return pluginSets;
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "plugins")
  @Getter
  @AllArgsConstructor
  public static class Plugin {

    private final String name;
    private final String version;
    private final String displayName;
    private final String description;
    private final String category;
    private final String author;
    private final String avatarUrl;
    private final String sha256sum;
    private PluginInformation.PluginType type;

    @XmlElement(name = "conditions")
    private final Condition conditions;

    @XmlElement(name = "dependencies")
    private final Set<String> dependencies;

    @XmlElement(name = "optionalDependencies")
    private final Set<String> optionalDependencies;

    @XmlElement(name = "_links")
    private final Map<String, Link> links;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "pluginSets")
  @Getter
  @AllArgsConstructor
  public static class PluginSet {
    private final String id;
    private final String versions;
    private final int sequence;

    @XmlElement(name = "plugins")
    private final Set<String> plugins;

    @XmlElement(name = "descriptions")
    private final Map<String, Description> descriptions;

    @XmlElement(name = "images")
    private final Map<String, String> images;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Description {
    private String name;

    @XmlElement(name = "features")
    private List<String> features;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "conditions")
  @Getter
  @AllArgsConstructor
  public static class Condition {

    private final List<String> os;
    private final String arch;
    private final String minVersion;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  static class Link {
    private String href;
  }
}
