/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
