package sonia.scm.repository.xml;

import org.apache.commons.lang.StringUtils;
import sonia.scm.ModelObject;
import sonia.scm.repository.Repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "repository-link")
@XmlAccessorType(XmlAccessType.FIELD)
public class RepositoryPath implements ModelObject {

  private String path;
  private String id;
  private Long lastModified;
  private Long creationDate;

  @XmlTransient
  private Repository repository;

  @XmlTransient
  private boolean toBeSynchronized;

  /**
   * Needed from JAXB
   */
  public RepositoryPath() {
  }

  public RepositoryPath(String path, String id, Repository repository) {
    this.path = path;
    this.id = id;
    this.repository = repository;
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public Long getCreationDate() {
    return creationDate;
  }

  @Override
  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Long getLastModified() {
    return lastModified;
  }

  @Override
  public String getType() {
    return getRepository()!= null? getRepository().getType():"";
  }

  @Override
  public boolean isValid() {
    return StringUtils.isNotEmpty(path);
  }

  public boolean toBeSynchronized() {
    return toBeSynchronized;
  }

  public void setToBeSynchronized(boolean toBeSynchronized) {
    this.toBeSynchronized = toBeSynchronized;
  }
}
