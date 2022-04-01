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

package sonia.scm.repository.cli;

import com.google.common.annotations.VisibleForTesting;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import javax.inject.Inject;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Mapper
public abstract class RepositoryToRepositoryCommandDtoMapper {

  @Inject
  private RepositoryServiceFactory serviceFactory;

  public abstract RepositoryCommandDto map(Repository modelObject);

  @ObjectFactory
  RepositoryCommandDto createDto(Repository repository) {
    RepositoryCommandDto dto = new RepositoryCommandDto();
    try (RepositoryService service = serviceFactory.create(repository)) {
      Optional<ScmProtocol> protocolUrl = service.getSupportedProtocols().filter(p -> p.getType().equals("http")).findFirst();
      protocolUrl.ifPresent(scmProtocol -> dto.setUrl(scmProtocol.getUrl()));
    }
    return dto;
  }

  String mapTimestampToISODate(Long timestamp) {
    if (timestamp != null) {
      return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp));
    }
    return null;
  }

  @VisibleForTesting
  void setServiceFactory(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }
}
