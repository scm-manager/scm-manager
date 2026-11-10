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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorFilterTest {

  @Test
  void shouldDistinguishRemovalFromRejectionAndAcceptance() {
    assertThat(MirrorFilter.Result.accept().isAccepted()).isTrue();
    assertThat(MirrorFilter.Result.accept().isRemovalRequested()).isFalse();
    assertThat(MirrorFilter.Result.reject().isAccepted()).isFalse();
    assertThat(MirrorFilter.Result.reject().isRemovalRequested()).isFalse();
    assertThat(MirrorFilter.Result.remove().isAccepted()).isFalse();
    assertThat(MirrorFilter.Result.remove().isRemovalRequested()).isTrue();
    assertThat(MirrorFilter.Result.remove().getRejectReason()).isEmpty();
    assertThat(MirrorFilter.Result.reject("invalid signature").getRejectReason()).contains("invalid signature");
  }

  @Test
  void shouldKeepRemovalReasonSeparateFromRejectionReason() {
    MirrorFilter.Result result = MirrorFilter.Result.remove("excluded by name");

    assertThat(result.isRemovalRequested()).isTrue();
    assertThat(result.isAccepted()).isFalse();
    assertThat(result.getRemoveReason()).contains("excluded by name");
    assertThat(result.getRejectReason()).isEmpty();
  }

  @Test
  void shouldNotExposeRemovalReasonForOtherOutcomesOrWhenUnspecified() {
    assertThat(MirrorFilter.Result.accept().getRemoveReason()).isEmpty();
    assertThat(MirrorFilter.Result.reject("invalid signature").getRemoveReason()).isEmpty();
    assertThat(MirrorFilter.Result.remove().getRemoveReason()).isEmpty();
    assertThat(MirrorFilter.Result.remove(null).getRemoveReason()).isEmpty();
  }
}
