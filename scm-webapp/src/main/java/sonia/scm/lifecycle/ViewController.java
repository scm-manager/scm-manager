package sonia.scm.lifecycle;

import javax.servlet.http.HttpServletRequest;

public interface ViewController {

  String getTemplate();

  View createView(HttpServletRequest request);

}
