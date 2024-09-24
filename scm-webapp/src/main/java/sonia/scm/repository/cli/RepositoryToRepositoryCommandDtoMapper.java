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

package sonia.scm.repository.cli;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Mapper
abstract class RepositoryToRepositoryCommandDtoMapper {

  @Inject
  private RepositoryServiceFactory serviceFactory;

  @Mapping(target = "url", ignore = true)
  abstract RepositoryCommandBean map(Repository modelObject);

  @ObjectFactory
  RepositoryCommandBean createBean(Repository repository) {
    RepositoryCommandBean bean = new RepositoryCommandBean();
    try (RepositoryService service = serviceFactory.create(repository)) {
      Optional<ScmProtocol> protocolUrl = service.getSupportedProtocols().filter(p -> p.getType().equals("http")).findFirst();
      protocolUrl.ifPresent(scmProtocol -> bean.setUrl(scmProtocol.getUrl()));
    }
    return bean;
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
