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
