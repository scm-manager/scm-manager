package sonia.scm.protocolcommand;

@FunctionalInterface
public interface CommandParser {

  String[] parse(String command);

}
