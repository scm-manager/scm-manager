package sonia.scm.repository;

import sonia.scm.NotFoundException;

public class BranchNotFoundException extends NotFoundException {
  public BranchNotFoundException(String branch) {
    super("branch", branch);
  }
}
