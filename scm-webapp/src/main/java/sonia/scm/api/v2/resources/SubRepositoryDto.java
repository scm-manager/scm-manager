package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubRepositoryDto {
  private String repositoryUrl;
  private String browserUrl;
  private String revision;
}
