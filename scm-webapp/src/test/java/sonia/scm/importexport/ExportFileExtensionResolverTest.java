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
