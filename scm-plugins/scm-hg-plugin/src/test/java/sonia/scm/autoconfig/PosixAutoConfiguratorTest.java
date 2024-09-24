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

package sonia.scm.autoconfig;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgVerifier;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PosixAutoConfiguratorTest {

  @Mock
  private HgVerifier verifier;

  @Test
  void shouldConfigureMercurial(@TempDir Path directory) {
    Path hg = directory.resolve("hg");
    when(verifier.isValid(hg)).thenReturn(true);

    PosixAutoConfigurator configurator = create(directory);

    HgGlobalConfig config = new HgGlobalConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
  }

  private PosixAutoConfigurator create(Path directory) {
    return new PosixAutoConfigurator(verifier, createEnv(directory), Collections.emptyList());
  }

  private Map<String, String> createEnv(Path... paths) {
    return ImmutableMap.of("PATH", Joiner.on(File.pathSeparator).join(paths));
  }


  @Test
  void shouldFindMercurialInAdditionalPath(@TempDir Path directory) {
    Path def = directory.resolve("default");
    Path additional = directory.resolve("additional");
    Path hg = def.resolve("hg");

    when(verifier.isValid(hg)).thenReturn(true);

    PosixAutoConfigurator configurator = new PosixAutoConfigurator(
      verifier, createEnv(def), ImmutableList.of(additional.toAbsolutePath().toString())
    );

    HgGlobalConfig config = new HgGlobalConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
  }

  @Test
  void shouldSkipInvalidMercurialInstallations(@TempDir Path directory) {
    Path one = directory.resolve("one");
    Path two = directory.resolve("two");
    Path three = directory.resolve("three");
    Path hg = three.resolve("hg");

    when(verifier.isValid(any(Path.class))).then(ic -> {
      Path path = ic.getArgument(0, Path.class);
      return path.equals(hg);
    });

    PosixAutoConfigurator configurator = new PosixAutoConfigurator(
      verifier, createEnv(one), ImmutableList.of(
      two.toAbsolutePath().toString(),
      three.toAbsolutePath().toString()
    )
    );

    HgGlobalConfig config = new HgGlobalConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
  }

  @Test
  void shouldNotConfigureMercurial(@TempDir Path directory) {
    PosixAutoConfigurator configurator = create(directory);

    HgGlobalConfig config = new HgGlobalConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isNull();
  }

}
