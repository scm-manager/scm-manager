package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
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
  // REVIEW files nicht embedded?
  private List<FileObjectDto> files;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  // REVIEW return null?
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
