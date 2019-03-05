package sonia.scm.protocolcommand;

import sonia.scm.plugin.ExtensionPoint;

import java.util.Optional;

@ExtensionPoint
public interface CommandInterpreterFactory {
  Optional<CommandInterpreter> canHandle(String command);
}
