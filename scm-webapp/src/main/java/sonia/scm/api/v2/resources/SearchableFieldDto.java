package sonia.scm.api.v2.resources;

import lombok.Data;

@Data
public class SearchableFieldDto {
  private String name;
  private String type;
}
