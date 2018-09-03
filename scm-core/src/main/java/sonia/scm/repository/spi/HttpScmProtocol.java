package sonia.scm.repository.spi;

import sonia.scm.repository.api.ScmProtocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpScmProtocol extends ScmProtocol {
  @Override
  default String getType() {
    return "http";
  }

  void serve(HttpServletRequest request, HttpServletResponse response);
}
