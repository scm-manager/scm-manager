package sonia.scm.repository.spi;

public interface MergeCommand {
  boolean merge(MergeCommandRequest request);
}
