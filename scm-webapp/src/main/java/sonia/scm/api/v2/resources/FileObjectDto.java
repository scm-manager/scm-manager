package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FileObjectDto extends HalRepresentation {
  private String name;
  private String path;
  private boolean directory;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String description;
  private long length;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Instant lastModified;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private SubRepositoryDto subRepository;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String revision;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  public void setChildren(List<FileObjectDto> children) {
    if (!children.isEmpty()) {
      // prevent empty embedded attribute in json
      this.withEmbedded("children", children);
    }
  }
}
