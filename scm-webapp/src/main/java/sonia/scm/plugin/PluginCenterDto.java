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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
