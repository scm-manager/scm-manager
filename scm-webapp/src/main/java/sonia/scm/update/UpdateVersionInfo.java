package sonia.scm.update;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "latest-version")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateVersionInfo {
  private String latestVersion;

  public UpdateVersionInfo() {
  }

  public UpdateVersionInfo(String latestVersion) {
    this.latestVersion = latestVersion;
  }

  public String getLatestVersion() {
    return latestVersion;
  }
}
