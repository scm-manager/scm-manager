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
    private String sha256;

    @XmlElement(name = "conditions")
    private Condition conditions;

    @XmlElement(name = "dependencies")
    private Set<String> dependencies;

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
