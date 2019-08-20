package sonia.scm.plugin;

public class PluginFailedToCancelInstallationException extends RuntimeException {
  public PluginFailedToCancelInstallationException(String message) {
    super(message);
  }
}
