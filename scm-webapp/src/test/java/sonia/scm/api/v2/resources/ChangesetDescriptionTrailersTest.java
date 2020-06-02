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

package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.Trailer;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChangesetDescriptionTrailersTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private final ChangesetDescriptionTrailers changesetDescriptionTrailers = new ChangesetDescriptionTrailers();

  @Test
  void shouldReturnEmptyList() {
    Changeset changeset = createChangeset("zaphod beeblebrox");

    List<Trailer> trailer = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    assertThat(trailer).isNotNull();
    assertThat(trailer).isEmpty();
  }

  @Test
  void shouldReturnTrailerWithCoAuthors() {
    DisplayUser displayUser = createDisplayUser("Arthur Dent", "dent@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCo-authored-by: Arthur Dent <dent@hitchhiker.org>");

    List<Trailer> Trailer = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    Trailer first = Trailer.get(0);
    assertThat(first.getTrailerType()).isEqualTo("Co-authored-by");
    assertThat(first.getPerson().getName()).isEqualTo(displayUser.getDisplayName());
    assertThat(first.getPerson().getMail()).isEqualTo(displayUser.getMail());
  }

  @Test
  void shouldReturnTrailerWithReviewers() {
    DisplayUser displayUser = createDisplayUser("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\nReviewed-by: Tricia McMillan <trillian@hitchhiker.org>");

    List<Trailer> trailer = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    Trailer Trailer = trailer.get(0);

    assertThat(Trailer.getTrailerType()).isEqualTo("Reviewed-by");
    assertThat(Trailer.getPerson().getName()).isEqualTo(displayUser.getDisplayName());
    assertThat(Trailer.getPerson().getMail()).isEqualTo(displayUser.getMail());
  }

  @Test
  void shouldReturnTrailerWithSigner() {
    DisplayUser displayUser = createDisplayUser("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\nSigned-off-by: Tricia McMillan <trillian@hitchhiker.org>");

    List<Trailer> trailer = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    Trailer Trailer = trailer.get(0);

    assertThat(Trailer.getTrailerType()).isEqualTo("Signed-off-by");
    assertThat(Trailer.getPerson().getName()).isEqualTo(displayUser.getDisplayName());
    assertThat(Trailer.getPerson().getMail()).isEqualTo(displayUser.getMail());
  }

  @Test
  void shouldReturnTrailerWithCommitter() {
    DisplayUser displayUser = createDisplayUser("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\nCommitted-by: Tricia McMillan <trillian@hitchhiker.org>");

    List<Trailer> trailer = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    Trailer Trailer = trailer.get(0);

    assertThat(Trailer.getTrailerType()).isEqualTo("Committed-by");
    assertThat(Trailer.getPerson().getName()).isEqualTo(displayUser.getDisplayName());
    assertThat(Trailer.getPerson().getMail()).isEqualTo(displayUser.getMail());
  }

  private Changeset createChangeset(String commitMessage) {
    Changeset changeset = new Changeset();
    changeset.setDescription(commitMessage);
    return changeset;
  }

  private DisplayUser createDisplayUser(String name, String mail) {
    return DisplayUser.from(new User(name, name, mail));
  }
}
