package sonia.scm.lifecycle;

class View {

  private final int statusCode;
  private final Object model;

  View(int statusCode, Object model) {
    this.statusCode = statusCode;
    this.model = model;
  }

  int getStatusCode() {
    return statusCode;
  }

  Object getModel() {
    return model;
  }
}
