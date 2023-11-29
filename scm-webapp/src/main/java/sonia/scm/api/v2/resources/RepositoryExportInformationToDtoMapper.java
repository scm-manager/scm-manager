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
