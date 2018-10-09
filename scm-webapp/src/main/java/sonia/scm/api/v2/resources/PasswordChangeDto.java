package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString
public class PasswordChangeDto {

  private String oldPassword;

  @NotEmpty
  private String newPassword;
}
