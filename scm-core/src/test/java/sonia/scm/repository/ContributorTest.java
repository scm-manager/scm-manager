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
