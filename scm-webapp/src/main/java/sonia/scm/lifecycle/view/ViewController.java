package sonia.scm.lifecycle.view;

import javax.servlet.http.HttpServletRequest;

public interface ViewController {

  String getTemplate();

  View createView(HttpServletRequest request);

}
