package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import java.io.File;

@Getter
@Setter
public abstract class SimpleRepositoryConfigDto extends HalRepresentation {

  private boolean disabled = false;
  @XmlElement(name = "repository-directory")
  private File repositoryDirectory;
}
