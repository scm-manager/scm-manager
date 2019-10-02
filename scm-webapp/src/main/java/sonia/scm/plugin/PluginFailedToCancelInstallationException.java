package sonia.scm.plugin;

import sonia.scm.ExceptionWithContext;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class PluginFailedToCancelInstallationException extends ExceptionWithContext {
  public PluginFailedToCancelInstallationException(String message, String name, Exception cause) {
    super(entity("plugin", name).build(), message, cause);
  }

  @Override
  public String getCode() {
    return "65RdZ5atX1";
  }
}
