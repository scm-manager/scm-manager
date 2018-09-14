package sonia.scm.web;

import com.google.inject.Inject;

import javax.inject.Provider;

public class ScmGitServletProvider extends ScmProviderHttpServletProvider {

  @Inject
  private Provider<ScmGitServlet> scmGitServlet;

  @Override
  protected ScmGitServlet getRootServlet() {
    return scmGitServlet.get();
  }
}
