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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChangesetDescriptionTrailerProviderTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private final ChangesetDescriptionTrailerProvider changesetDescriptionTrailers = new ChangesetDescriptionTrailerProvider();

  @Test
  void shouldReturnEmptyList() {
    Changeset changeset = createChangeset("zaphod beeblebrox");

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    assertThat(trailers).isNotNull();
    assertThat(trailers).isEmpty();
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox");
  }

  @Test
  void shouldConvertTrailerWithCoAuthors() {
    Person person = createPerson("Arthur Dent", "dent@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCo-authored-by: Arthur Dent <dent@hitchhiker.org>");

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    Trailer trailer = trailers.iterator().next();
    assertThat(trailer.getTrailerType()).isEqualTo("Co-authored-by");
    assertThat(trailer.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldConvertTrailerWithReviewers() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nReviewed-by: Tricia McMillan <trillian@hitchhiker.org>");

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    Trailer trailer = trailers.iterator().next();

    assertThat(trailer.getTrailerType()).isEqualTo("Reviewed-by");
    assertThat(trailer.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldConvertTrailerWithSigner() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\n\nSigned-off-by: Tricia McMillan <trillian@hitchhiker.org>");

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    Trailer trailer = trailers.iterator().next();

    assertThat(trailer.getTrailerType()).isEqualTo("Signed-off-by");
    assertThat(trailer.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n\n");
  }

  @Test
  void shouldConvertTrailerWithCommitter() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCommitted-by: Tricia McMillan <trillian@hitchhiker.org>");

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    Trailer trailer = trailers.iterator().next();

    assertThat(trailer.getTrailerType()).isEqualTo("Committed-by");
    assertThat(trailer.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldConvertMixedTrailers() {
    Changeset changeset = createChangeset("zaphod beeblebrox\n\n" +
      "Committed-by: Tricia McMillan <trillian@hitchhiker.org>\n" +
      "Signed-off-by: Artur Dent <dent@hitchhiker.org>");

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    Iterator<Trailer> trailerIterator = trailers.iterator();
    Trailer firstTrailer = trailerIterator.next();
    Trailer secondTrailer = trailerIterator.next();

    assertThat(firstTrailer.getTrailerType()).isEqualTo("Committed-by");
    assertThat(firstTrailer.getPerson())
      .isEqualTo(createPerson("Tricia McMillan", "trillian@hitchhiker.org"));

    assertThat(secondTrailer.getTrailerType()).isEqualTo("Signed-off-by");
    assertThat(secondTrailer.getPerson())
      .isEqualTo(createPerson("Artur Dent", "dent@hitchhiker.org"));

    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldNotTouchUnknownTrailers() {
    String originalCommitMessage = "zaphod beeblebrox\n\n" +
      "Some-strange-tag: Tricia McMillan <trillian@hitchhiker.org>";
    Changeset changeset = createChangeset(originalCommitMessage);

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    assertThat(trailers).isEmpty();
    assertThat(changeset.getDescription()).isEqualTo(originalCommitMessage);
  }

  @Test
  void shouldIgnoreKnownTrailersWithIllegalNameFormat() {
    String originalCommitMessage = "zaphod beeblebrox\n\n" +
      "Committed-by: Tricia McMillan";
    Changeset changeset = createChangeset(originalCommitMessage);

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    assertThat(trailers).isEmpty();
    assertThat(changeset.getDescription()).isEqualTo(originalCommitMessage);
  }

  @Test
  void shouldIgnoreWhitespacesInEmptyLines() {
    String originalCommitMessage = "zaphod beeblebrox\n    \n" +
      "Committed-by: Tricia McMillan <trillian@hitchhiker.org>";
    Changeset changeset = createChangeset(originalCommitMessage);

    changesetDescriptionTrailers.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Trailer> trailers = changeset.getTrailers();

    assertThat(trailers).isNotEmpty();
  }

  private Changeset createChangeset(String commitMessage) {
    Changeset changeset = new Changeset();
    changeset.setDescription(commitMessage);
    return changeset;
  }

  private Person createPerson(String name, String mail) {
    return new Person(name, mail);
  }
}
