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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BundleCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.BundleCommand;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFileExtensionResolverTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private BundleCommandBuilder bundleCommand;

  @InjectMocks
  private ExportFileExtensionResolver resolver;

  @Test
  void shouldResolveWithMetadata() {
    String result = resolver.resolve(REPOSITORY, true, false, false);

    assertThat(result).isEqualTo("tar.gz");
  }

  @Test
  void shouldResolveWithMetadataAndEncrypted() {
    String result = resolver.resolve(REPOSITORY, true, false, true);

    assertThat(result).isEqualTo("tar.gz.enc");
  }

  @Nested
  class withRepositoryService {

    @BeforeEach
    void initBundleCommand() {
      when(serviceFactory.create(REPOSITORY)).thenReturn(service);
      when(service.getBundleCommand()).thenReturn(bundleCommand);
      when(bundleCommand.getFileExtension()).thenReturn("dump");
    }

    @Test
    void shouldResolveDump() {
      String result = resolver.resolve(REPOSITORY, false, false, false);

      assertThat(result).isEqualTo("dump");
    }

    @Test
    void shouldResolveDump_Compressed() {
      String result = resolver.resolve(REPOSITORY, false, true, false);

      assertThat(result).isEqualTo("dump.gz");
    }

    @Test
    void shouldResolveDump_Encrypted() {
      String result = resolver.resolve(REPOSITORY, false, false, true);

      assertThat(result).isEqualTo("dump.enc");
    }

    @Test
    void shouldResolveDump_Compressed_Encrypted() {
      String result = resolver.resolve(REPOSITORY, false, true, true);

      assertThat(result).isEqualTo("dump.gz.enc");
    }
  }
}
