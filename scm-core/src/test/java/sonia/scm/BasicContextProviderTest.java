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

package sonia.scm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.BasicContextProvider.DEVELOPMENT_INSTANCE_ID;
import static sonia.scm.BasicContextProvider.STAGE_PROPERTY;

class BasicContextProviderTest {

  @Nested
  class VersionTests {

    @Test
    void shouldReturnVersionFromSystemProperty() {
      System.setProperty(BasicContextProvider.VERSION_PROPERTY, "3.0.0");
      try {
        SCMContextProvider context = new BasicContextProvider();
        assertThat(context.getVersion()).isEqualTo("3.0.0");
      } finally {
        System.clearProperty(BasicContextProvider.VERSION_PROPERTY);
      }
    }

    @Test
    void shouldReturnDefaultVersion() {
      SCMContextProvider context = new BasicContextProvider();
      assertThat(context.getVersion()).isEqualTo(BasicContextProvider.VERSION_DEFAULT);
    }

  }

  @Nested
  class PathTests {

    private Path baseDirectory;

    private BasicContextProvider context;

    @BeforeEach
    void setUpContext(@TempDir Path baseDirectory) {
      this.baseDirectory = baseDirectory;
      context = new BasicContextProvider(baseDirectory.toFile(), "x.y.z", Stage.PRODUCTION);
    }

    @Test
    void shouldReturnAbsolutePathAsIs(@TempDir Path path) {
      Path absolutePath = path.toAbsolutePath();
      Path resolved = context.resolve(absolutePath);

      assertThat(resolved).isSameAs(absolutePath);
    }

    @Test
    void shouldResolveRelatePath() {
      Path path = Paths.get("repos", "42");
      Path resolved = context.resolve(path);

      assertThat(resolved).isAbsolute();
      assertThat(resolved).startsWithRaw(baseDirectory);
      assertThat(resolved).endsWithRaw(path);
    }

  }

  @Nested
  class InstanceIdTests {

    private String originalProperty;

    @BeforeEach
    void setUp() {
      originalProperty = System.getProperty(BasicContextProvider.DIRECTORY_PROPERTY);
    }

    @AfterEach
    void tearDown() {
      if (originalProperty != null) {
        System.setProperty(BasicContextProvider.DIRECTORY_PROPERTY, originalProperty);
      }
    }

    @Test
    void shouldReturnInstanceId(@TempDir Path baseDirectory) {
      System.setProperty(BasicContextProvider.DIRECTORY_PROPERTY, baseDirectory.toString());
      BasicContextProvider provider = new BasicContextProvider();

      assertThat(provider.getInstanceId()).isNotBlank();
    }

    @Test
    void shouldReturnPersistedInstanceId(@TempDir Path baseDirectory) {
      System.setProperty(BasicContextProvider.DIRECTORY_PROPERTY, baseDirectory.toString());
      BasicContextProvider provider = new BasicContextProvider();

      String firstInstanceId = provider.getInstanceId();

      provider = new BasicContextProvider();

      assertThat(provider.getInstanceId()).isEqualTo(firstInstanceId);
    }

    @Nested
    class WithStageDevelopment {
      @BeforeEach
      void setStage() {
        System.setProperty(STAGE_PROPERTY, Stage.DEVELOPMENT.name());
      }

      @Test
      void shouldReturnHardCodeInstanceIfIfStageDevelopment() {
        BasicContextProvider basicContextProvider = new BasicContextProvider();

        String instanceId = basicContextProvider.getInstanceId();

        assertThat(instanceId).isEqualTo(DEVELOPMENT_INSTANCE_ID);
      }

      @AfterEach
      void resetStage() {
        System.setProperty(STAGE_PROPERTY, Stage.PRODUCTION.name());
      }
    }
  }
}
