package sonia.scm.repository.api;

public interface DiffResult extends Iterable<DiffFile> {

  String getOldRevision();

  String getNewRevision();
}
