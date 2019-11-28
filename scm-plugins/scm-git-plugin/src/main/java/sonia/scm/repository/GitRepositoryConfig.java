package sonia.scm.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class GitRepositoryConfig {

  public GitRepositoryConfig() {
  }

  public GitRepositoryConfig(String defaultBranch) {
    this.defaultBranch = defaultBranch;
  }

  private String defaultBranch;

  public String getDefaultBranch() {
    return defaultBranch;
  }

  public void setDefaultBranch(String defaultBranch) {
    this.defaultBranch = defaultBranch;
  }
}
