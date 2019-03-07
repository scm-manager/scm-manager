package sonia.scm.protocolcommand;

public interface CommandInterpreter {

  String[] getParsedArgs();

  ScmCommandProtocol getProtocolHandler();

  RepositoryContextResolver getRepositoryContextResolver();
}
