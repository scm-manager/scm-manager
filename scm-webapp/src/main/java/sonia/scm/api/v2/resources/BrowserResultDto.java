package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
public class BrowserResultDto extends HalRepresentation {
  private String revision;
  private String tag;
  private String branch;
  private Collection<FileObjectDto> files;
}
