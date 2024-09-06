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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.importexport.ExportService;
import sonia.scm.importexport.ExportStatus;
import sonia.scm.importexport.RepositoryExportInformation;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryExportInformationToDtoMapperTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/scm/api/"));

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ExportService exportService;

  private RepositoryExportInformationToDtoMapperImpl mapper;

  @BeforeEach
  void initResourceLinks() {
    mapper = new RepositoryExportInformationToDtoMapperImpl();
    mapper.setResourceLinks(resourceLinks);
    mapper.setExportService(exportService);
  }

  @Test
  void shouldMapToInfoDtoWithLinks() {
    when(exportService.isExporting(REPOSITORY)).thenReturn(false);
    when(exportService.getExportInformation(REPOSITORY).getStatus()).thenReturn(ExportStatus.FINISHED);

    String exporterName = "trillian";
    Instant now = Instant.now();
    RepositoryExportInformation info = new RepositoryExportInformation(exporterName, now, true, true, false, ExportStatus.FINISHED);

    RepositoryExportInformationDto dto = mapper.map(info, REPOSITORY);

    assertThat(dto.getExporterName()).isEqualTo(exporterName);
    assertThat(dto.getCreated()).isEqualTo(now);
    assertThat(dto.isCompressed()).isTrue();
    assertThat(dto.isWithMetadata()).isTrue();
    assertThat(dto.isEncrypted()).isFalse();
    assertThat(dto.getStatus()).isEqualTo(ExportStatus.FINISHED);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/scm/api/v2/repositories/hitchhiker/HeartOfGold/export/info");
    assertThat(dto.getLinks().getLinkBy("download").get().getHref()).isEqualTo("/scm/api/v2/repositories/hitchhiker/HeartOfGold/export/download");
  }

  @Test
  void shouldNotAppendDownloadLink() {
    when(exportService.isExporting(REPOSITORY)).thenReturn(true);

    String exporterName = "trillian";
    Instant now = Instant.now();
    RepositoryExportInformation info = new RepositoryExportInformation(exporterName, now, true, true, false, ExportStatus.EXPORTING);

    RepositoryExportInformationDto dto = mapper.map(info, REPOSITORY);

    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/scm/api/v2/repositories/hitchhiker/HeartOfGold/export/info");
    assertThat(dto.getLinks().getLinkBy("download")).isNotPresent();
  }
}
