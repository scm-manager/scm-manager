package sonia.scm.protocolcommand;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;
import java.io.OutputStream;

@Getter
@AllArgsConstructor
public class CommandContext {

  private String command;
  private String[] args;

  private InputStream inputStream;
  private OutputStream outputStream;
  private OutputStream errorStream;

}
