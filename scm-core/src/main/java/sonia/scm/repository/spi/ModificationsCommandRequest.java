package sonia.scm.repository.spi;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModificationsCommandRequest implements Resetable {
  private String revision;

  @Override
  public void reset() {
    revision = null;
  }
}
