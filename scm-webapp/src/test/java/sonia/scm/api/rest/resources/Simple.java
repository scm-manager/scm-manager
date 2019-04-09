package sonia.scm.api.rest.resources;

import sonia.scm.ModelObject;

public class Simple implements ModelObject {

  private String id;
  private String data;

  public Simple(String id, String data) {
    this.id = id;
    this.data = data;
  }

  public String getData() {
    return data;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setLastModified(Long timestamp) {

  }

  @Override
  public Long getCreationDate() {
    return null;
  }

  @Override
  public void setCreationDate(Long timestamp) {

  }

  @Override
  public Long getLastModified() {
    return null;
  }

  @Override
  public String getType() {
    return null;
  }
  @Override
  public boolean isValid() {
    return false;
  }
}
