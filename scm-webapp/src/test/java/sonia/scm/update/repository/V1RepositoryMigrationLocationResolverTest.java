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

package sonia.scm.update.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class V1RepositoryMigrationLocationResolverTest {

  @Mock
  SCMContextProvider contextProvider;

  private Path scmBaseDirectory;
  private Path configDirectory;

  @BeforeEach
  void initContext(@TempDir Path temp) throws IOException {
    scmBaseDirectory = temp.resolve("scm");
    configDirectory = scmBaseDirectory.resolve("config");
    Files.createDirectories(configDirectory);
    when(contextProvider.getBaseDirectory()).thenReturn(scmBaseDirectory.toFile());
  }

  @Test
  void shouldReturnDefaultPathIfNothingIsConfigured() {
    V1RepositoryMigrationLocationResolver resolver = new V1RepositoryMigrationLocationResolver(contextProvider);

    Path path = resolver.getTypeDependentPath("git");

    assertThat(path).isEqualTo(scmBaseDirectory.resolve("repositories").resolve("git"));
  }

  @Test
  void shouldReturnCustomPathIfAnotherPathIsConfiguredForGit(@TempDir Path temp) throws IOException {
    Path otherGitDirectory = temp.resolve("other");
    Files.write(configDirectory.resolve("git.xml"),
      asList("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>",
        "<config>",
        "    <disabled>false</disabled>",
        "    <repositoryDirectory>" + otherGitDirectory + "</repositoryDirectory>",
        "    <gc-expression></gc-expression>",
        "    <disallow-non-fast-forward>false</disallow-non-fast-forward>",
        "</config>"
      )
    );

    Path path = new V1RepositoryMigrationLocationResolver(contextProvider).getTypeDependentPath("git");

    assertThat(path).isEqualTo(otherGitDirectory);
  }

  @Test
  void shouldReturnCustomPathIfAnotherPathIsConfiguredForHg(@TempDir Path temp) throws IOException {
    Path otherHgDirectory = temp.resolve("other");
    Files.write(configDirectory.resolve("hg.xml"),
      asList("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>",
        "<config>",
        "    <disabled>false</disabled>",
        "    <repositoryDirectory>/var/lib/scm/repositories/hg</repositoryDirectory>",
        "    <repositoryDirectory>" + otherHgDirectory + "</repositoryDirectory>",
        "    <disableHookSSLValidation>false</disableHookSSLValidation>",
        "    <enableHttpPostArgs>false</enableHttpPostArgs>",
        "    <encoding>UTF-8</encoding>",
        "    <hgBinary>hg</hgBinary>",
        "    <pythonBinary>python2</pythonBinary>",
        "    <pythonPath></pythonPath>",
        "    <showRevisionInId>false</showRevisionInId>",
        "    <useOptimizedBytecode>false</useOptimizedBytecode>",
        "</config>"
      )
    );

    Path path = new V1RepositoryMigrationLocationResolver(contextProvider).getTypeDependentPath("hg");

    assertThat(path).isEqualTo(otherHgDirectory);
  }

  @Test
  void shouldReturnCustomPathIfAnotherPathIsConfiguredForSvn(@TempDir Path temp) throws IOException {
    Path otherSvnDirectory = temp.resolve("other");
    Files.write(configDirectory.resolve("svn.xml"),
      asList("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>",
        "<config>",
        "    <disabled>false</disabled>",
        "    <repositoryDirectory>/var/lib/scm/repositories/svn</repositoryDirectory>",
        "    <repositoryDirectory>" + otherSvnDirectory + "</repositoryDirectory>",
        "    <enable-gzip>false</enable-gzip>",
        "    <compatibility>NONE</compatibility>",
        "</config>"
      )
    );

    Path path = new V1RepositoryMigrationLocationResolver(contextProvider).getTypeDependentPath("svn");

    assertThat(path).isEqualTo(otherSvnDirectory);
  }

  @Test
  void shouldReturnDefaultPathForUnknownRepositoryType() {
    Path path = new V1RepositoryMigrationLocationResolver(contextProvider).getTypeDependentPath("other");

    assertThat(path).isEqualTo(scmBaseDirectory.resolve("repositories").resolve("other"));
  }
}
