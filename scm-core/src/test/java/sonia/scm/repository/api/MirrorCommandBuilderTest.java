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

package sonia.scm.repository.api;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.MirrorCommand;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MirrorCommandBuilderTest {

  private final MirrorCommand command = mock(MirrorCommand.class);
  private final MirrorCommandBuilder builder = new MirrorCommandBuilder(command, new Repository("id", "git", "space", "mirror"));

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldLogSourceForInitialAndSubsequentSynchronizations(boolean initial) {
    MirrorCommandResult original = result(MirrorCommandResult.ResultType.OK);
    when(command.mirror(any())).thenReturn(original);
    when(command.update(any())).thenReturn(original);
    builder.setSourceUrl("https://source.example/repository");

    MirrorCommandResult result = initial ? builder.initialCall() : builder.update();

    assertThat(result.getLog()).containsExactly("Pulling changes from https://source.example/repository", "No updates found");
    assertThat(original.getLog()).containsExactly("No updates found");
    assertThat(result.getDuration()).isEqualTo(original.getDuration());
    assertThat(result.getLfsUpdateResult()).isSameAs(original.getLfsUpdateResult());
  }

  @ParameterizedTest
  @EnumSource(MirrorCommandResult.ResultType.class)
  void shouldPreserveResultStatus(MirrorCommandResult.ResultType status) {
    when(command.update(any())).thenReturn(result(status));
    builder.setSourceUrl("https://source.example/repository");

    MirrorCommandResult result = builder.update();

    assertThat(result.getResult()).isEqualTo(status);
    assertThat(result.getLog()).startsWith("Pulling changes from https://source.example/repository");
  }

  private MirrorCommandResult result(MirrorCommandResult.ResultType status) {
    return new MirrorCommandResult(status, List.of("No updates found"), Duration.ofSeconds(3), new MirrorCommandResult.LfsUpdateResult());
  }
}
