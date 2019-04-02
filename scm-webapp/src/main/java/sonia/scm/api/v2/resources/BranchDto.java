package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

@Getter @Setter @NoArgsConstructor
public class BranchDto extends HalRepresentation {

  private static final String VALID_CHARACTERS_AT_START_AND_END = "\\w-,;\\]{}@&+=$#`|<>";
  private static final String VALID_CHARACTERS = VALID_CHARACTERS_AT_START_AND_END + "/.";
  static final String VALID_BRANCH_NAMES = "[" + VALID_CHARACTERS_AT_START_AND_END + "]([" + VALID_CHARACTERS + "]*[" + VALID_CHARACTERS_AT_START_AND_END + "])?";

  @NotEmpty @Length(min = 1, max=100) @Pattern(regexp = VALID_BRANCH_NAMES)
  private String name;
  private String revision;
  private boolean defaultBranch;

  BranchDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
