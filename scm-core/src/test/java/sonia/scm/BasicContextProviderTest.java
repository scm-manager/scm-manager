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

package sonia.scm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.config.WebappConfigProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.BasicContextProvider.DEVELOPMENT_INSTANCE_ID;

class BasicContextProviderTest {

  @Nested
  class VersionTests {

    @Test
    void shouldReturnVersionFromVersionOverride() {
      WebappConfigProvider.setConfigBindings(Map.of("versionOverride", "3.0.0"));
      SCMContextProvider context = new BasicContextProvider();
      assertThat(context.getVersion()).isEqualTo("3.0.0");
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

      @Test
      void shouldReturnHardCodeInstanceIfIfStageDevelopment() {
        WebappConfigProvider.setConfigBindings(Map.of("stage", Stage.DEVELOPMENT.name()));

        BasicContextProvider basicContextProvider = new BasicContextProvider();
        String instanceId = basicContextProvider.getInstanceId();

        assertThat(instanceId).isEqualTo(DEVELOPMENT_INSTANCE_ID);
      }
    }
  }
}
