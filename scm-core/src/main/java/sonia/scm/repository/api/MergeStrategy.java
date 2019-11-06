package sonia.scm.repository.api;

public enum MergeStrategy {
  SQUASH,
  MERGE_COMMIT,
  FAST_FORWARD_IF_POSSIBLE
}
