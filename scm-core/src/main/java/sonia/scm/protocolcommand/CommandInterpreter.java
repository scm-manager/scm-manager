package sonia.scm.protocolcommand;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface CommandInterpreter {

  boolean canHandle(String command);

  CommandParser getParser();

  ScmCommandProtocol getProtocolHandler();
}
