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

package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.importexport.ExportService;
import sonia.scm.importexport.ExportStatus;
import sonia.scm.importexport.RepositoryExportInformation;
import sonia.scm.repository.Repository;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class RepositoryExportInformationToDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private ExportService exportService;

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  @VisibleForTesting
  void setExportService(ExportService exportService) {
    this.exportService = exportService;
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  abstract RepositoryExportInformationDto map(RepositoryExportInformation info, @Context Repository repository);

  @ObjectFactory
  RepositoryExportInformationDto createDto(@Context Repository repository) {
    Links.Builder links = Links.linkingTo();
    links.self(resourceLinks.repository().exportInfo(repository.getNamespace(), repository.getName()));
    if (!exportService.isExporting(repository) && exportService.getExportInformation(repository).getStatus() == ExportStatus.FINISHED) {
      links.single(link("download", resourceLinks.repository().downloadExport(repository.getNamespace(), repository.getName())));
    }
    return new RepositoryExportInformationDto(links.build());
  }
}
