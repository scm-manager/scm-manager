package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BrowserResultDto extends HalRepresentation implements Iterable<FileObjectDto> {
  private String revision;
  private String tag;
  private String branch;
  private List<FileObjectDto> files;

  @Override
  public Iterator<FileObjectDto> iterator() {
    Iterator<FileObjectDto> it = null;

    if (files != null)
    {
      it = files.iterator();
    }

    return it;
  }
}
