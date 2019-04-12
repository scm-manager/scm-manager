package sonia.scm.api.v2.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RedirectAfterLogoutDto {
  private String logoutRedirect;
}
