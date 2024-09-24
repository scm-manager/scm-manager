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

package sonia.scm.repository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ContributorTest {

  @Test
  void shouldParseCoAuthoredBy() {
    Optional<Contributor> contributor = Contributor.fromCommitLine("Co-authored-by: Arthur Dent <dent@hitchhiker.org>");

    assertThat(contributor).get().isEqualTo(new Contributor(Contributor.CO_AUTHORED_BY, new Person("Arthur Dent", "dent@hitchhiker.org")));
  }

  @Test
  void shouldParseCommittedBy() {
    Optional<Contributor> contributor = Contributor.fromCommitLine("Committed-by: Arthur Dent <dent@hitchhiker.org>");

    assertThat(contributor).get().isEqualTo(new Contributor(Contributor.COMMITTED_BY, new Person("Arthur Dent", "dent@hitchhiker.org")));
  }

  @Test
  void shouldParseReviewedBy() {
    Optional<Contributor> contributor = Contributor.fromCommitLine("Reviewed-by: Arthur Dent <dent@hitchhiker.org>");

    assertThat(contributor).get().isEqualTo(new Contributor(Contributor.REVIEWED_BY, new Person("Arthur Dent", "dent@hitchhiker.org")));
  }

  @Test
  void shouldParseSignedOffBy() {
    Optional<Contributor> contributor = Contributor.fromCommitLine("Signed-off-by: Arthur Dent <dent@hitchhiker.org>");

    assertThat(contributor).get().isEqualTo(new Contributor(Contributor.SIGNED_OFF_BY, new Person("Arthur Dent", "dent@hitchhiker.org")));
  }

  @Test
  void shouldRenderCommitLine() {
    String commitLine = new Contributor(Contributor.CO_AUTHORED_BY, new Person("Arthur Dent", "dent@hitchhiker.org")).toCommitLine();

    assertThat(commitLine).isEqualTo("Co-authored-by: Arthur Dent <dent@hitchhiker.org>");
  }
}
