package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
    private String displayName;
    private String description;
    private String category;
    private String version;
    private String author;
    private String avatarUrl;
    private String sha256;

    @XmlElement(name = "conditions")
    private Condition conditions;

    @XmlElement(name = "dependecies")
    private Dependency dependencies;

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
  @XmlRootElement(name = "dependencies")
  @Getter
  @AllArgsConstructor
  static class Dependency {
    private String name;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  static class Link {
    private String href;
    private boolean templated;
  }
}
