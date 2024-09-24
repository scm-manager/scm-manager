/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.importexport;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.api.ExportFailedException;

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
  @Getter
  @XmlRootElement(name = "metadata")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class RepositoryMetadata {

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
