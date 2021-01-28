package sonia.scm.importexport;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.api.ExportFailedException;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

class RepositoryMetadataXmlGenerator {

  byte[] generate(Repository repository) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      RepositoryMetadata metadata = new RepositoryMetadata(repository);
      JAXB.marshal(metadata, baos);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new ExportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        "Could not generate SCM-Manager environment description.",
        e
      );
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @XmlRootElement(name = "metadata")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class RepositoryMetadata {

    private String namespace;
    private String name;
    private String type;
    private String contact;
    private String description;
    private Collection<RepositoryPermission> permissions;

    public RepositoryMetadata(Repository repository) {
      this(
        repository.getNamespace(),
        repository.getName(),
        repository.getType(),
        repository.getContact(),
        repository.getDescription(),
        repository.getPermissions());
    }
  }
}
