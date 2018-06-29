package sonia.scm.repository;

import sonia.scm.plugin.Extension;

@Extension
public class StaticSpacesStrategy implements SpacesStrategy {
  @Override
  public String getCurrentSpace() {
    return "test";
  }
}
