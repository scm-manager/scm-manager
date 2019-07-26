package sonia.scm.plugin;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

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
  static class Embedded {

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
  static class Plugin {

    private String name;
    private String displayName;
    private String description;
    private String category;
    private String version;
    private String author;
    private String sha256;

    @XmlElement(name = "conditions")
    private Condition conditions;

    @XmlElement(name = "dependecies")
    private Dependency dependencies;

    @XmlElement(name = "_links")
    private Links links;

  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "conditions")
  @Getter
  static class Condition {

    private String os;
    private String arch;
    private String minVersion;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "dependencies")
  @Getter
  static class Dependency {
    private String name;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "_links")
  @Getter
  static class Links {
    private String download;
  }

}
