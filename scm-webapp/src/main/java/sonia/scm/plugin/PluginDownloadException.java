package sonia.scm.plugin;

public class PluginDownloadException extends PluginInstallException {
  public PluginDownloadException(String message, Throwable cause) {
    super(message, cause);
  }
}
