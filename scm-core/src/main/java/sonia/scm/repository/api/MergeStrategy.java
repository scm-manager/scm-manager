package sonia.scm.repository.api;

public enum MergeStrategy {
  MERGE_COMMIT,
  FAST_FORWARD_IF_POSSIBLE,
  SQUASH
}
