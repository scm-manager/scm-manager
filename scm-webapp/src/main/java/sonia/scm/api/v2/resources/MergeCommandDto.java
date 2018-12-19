package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter @Setter
public class MergeCommandDto {

  @NotEmpty
  private String sourceRevision;
  @NotEmpty
  private String targetRevision;
}
