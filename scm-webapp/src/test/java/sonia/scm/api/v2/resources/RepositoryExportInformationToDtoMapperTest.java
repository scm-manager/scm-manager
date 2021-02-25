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
