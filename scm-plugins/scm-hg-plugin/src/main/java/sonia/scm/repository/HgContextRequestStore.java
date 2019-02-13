package sonia.scm.repository;

import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class HgContextRequestStore {

  private HgContext context = new HgContext();

  public HgContext get() {
    return context;
  }

}
