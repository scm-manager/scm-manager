package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HealthCheckFailureDto {
  private String description;
  private String summary;
  private String url;
}
