package sonia.scm.plugin;

public class PluginChecksumMismatchException extends PluginInstallException {
  public PluginChecksumMismatchException(String message) {
    super(message);
  }
}
