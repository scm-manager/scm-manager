package sonia.scm.repository.api;

public interface DiffFile extends Iterable<Hunk> {

  String getOldRevision();

  String getNewRevision();

  String getOldPath();

  String getNewPath();
}
