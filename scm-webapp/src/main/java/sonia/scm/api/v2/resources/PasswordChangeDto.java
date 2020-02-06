package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ToString
public class PasswordChangeDto {

  @NotEmpty
  private String oldPassword;

  @NotEmpty
  private String newPassword;
}
