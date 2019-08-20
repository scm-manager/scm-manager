package sonia.scm.plugin;

public class PluginInstallException extends RuntimeException {

  public PluginInstallException(String message) {
    super(message);
  }

  public PluginInstallException(String message, Throwable cause) {
    super(message, cause);
  }
}
