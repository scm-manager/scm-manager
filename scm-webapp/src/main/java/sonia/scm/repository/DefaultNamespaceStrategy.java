package sonia.scm.repository;

import sonia.scm.plugin.Extension;

@Extension
public class DefaultNamespaceStrategy implements NamespaceStrategy{
  @Override
  public String getNamespace() {
    return "42";
  }
}
