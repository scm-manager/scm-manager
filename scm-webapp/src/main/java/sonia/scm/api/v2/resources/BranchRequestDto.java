package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

import static sonia.scm.api.v2.resources.BranchDto.VALID_BRANCH_NAMES;

@Getter
@Setter
public class BranchRequestDto {

  @NotEmpty @Length(min = 1, max=100) @Pattern(regexp = VALID_BRANCH_NAMES)
  private String name;
  private String parent;
}
