package sonia.scm.repository.spi;

public interface MergeDryRunCommand {
  boolean isMergeable(MergeDryRunCommandRequest request);
}
