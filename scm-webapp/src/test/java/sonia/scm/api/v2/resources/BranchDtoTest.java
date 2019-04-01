package sonia.scm.api.v2.resources;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BranchDtoTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "v",
    "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
    "val#x",
    "val&x",
    "val+",
    "val,kill",
    "val.kill",
    "val;kill",
    "val<kill",
    "val=",
    "val>kill",
    "val@",
    "val]id",
    "val`id",
    "valid#",
    "valid.t",
    "val{",
    "val{d",
    "val{}d",
    "val|kill",
    "val}"
  })
  void shouldAcceptValidBranchName(String branchName) {
    assertTrue(branchName.matches(BranchDto.VALID_BRANCH_NAMES));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "",
    ".val",
    "val.",
    "/val",
    "val/",
    "val id"
  })
  void shouldRejectInvalidBranchName(String branchName) {
    assertFalse(branchName.matches(BranchDto.VALID_BRANCH_NAMES));
  }
}
