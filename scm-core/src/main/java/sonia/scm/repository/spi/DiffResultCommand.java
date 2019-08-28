package sonia.scm.repository.spi;

import sonia.scm.repository.api.DiffResult;

import java.io.IOException;

public interface DiffResultCommand {
  DiffResult getDiffResult(DiffCommandRequest request) throws IOException;
}
