package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonDto {

  /**
   * mail address of the person
   */
  private String mail;

  /**
   * name of the person
   */
  private String name;

}
