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

    List<TrailerPersonDto> trailerPersons = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    assertThat(trailerPersons).isNotNull();
    assertThat(trailerPersons).isEmpty();
  }

  @Test
  void shouldReturnTrailerPersonsWithCoAuthors() {
    DisplayUser displayUser = createDisplayUser("Arthur Dent", "dent@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCo-authored-by: Arthur Dent <dent@hitchhiker.org>");

    PersonDto personDto = createPersonDto(displayUser);

    List<TrailerPersonDto> trailerPersons = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    TrailerPersonDto trailerPerson = trailerPersons.get(0);
    assertThat(trailerPerson.getTrailerType()).isEqualTo("Co-authored-by");
    assertThat(trailerPerson.getName()).isEqualTo(personDto.getName());
    assertThat(trailerPerson.getMail()).isEqualTo(personDto.getMail());
  }

  @Test
  void shouldReturnTrailerPersonsWithReviewers() {
    DisplayUser displayUser = createDisplayUser("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\nReviewed-by: Tricia McMillan <trillian@hitchhiker.org>");

    PersonDto personDto = createPersonDto(displayUser);

    List<TrailerPersonDto> trailerPersons = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    TrailerPersonDto trailerPersonDto = trailerPersons.get(0);

    assertThat(trailerPersonDto.getTrailerType()).isEqualTo("Reviewed-by");
    assertThat(trailerPersonDto.getName()).isEqualTo(personDto.getName());
    assertThat(trailerPersonDto.getMail()).isEqualTo(personDto.getMail());
  }

  @Test
  void shouldReturnTrailerPersonsWithSigner() {
    DisplayUser displayUser = createDisplayUser("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\nSigned-off-by: Tricia McMillan <trillian@hitchhiker.org>");

    PersonDto personDto = createPersonDto(displayUser);

    List<TrailerPersonDto> trailerPersons = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    TrailerPersonDto trailerPersonDto = trailerPersons.get(0);

    assertThat(trailerPersonDto.getTrailerType()).isEqualTo("Signed-off-by");
    assertThat(trailerPersonDto.getName()).isEqualTo(personDto.getName());
    assertThat(trailerPersonDto.getMail()).isEqualTo(personDto.getMail());
  }

  @Test
  void shouldReturnTrailerPersonsWithCommitter() {
    DisplayUser displayUser = createDisplayUser("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\nCommitted-by: Tricia McMillan <trillian@hitchhiker.org>");

    PersonDto personDto = createPersonDto(displayUser);

    List<TrailerPersonDto> trailerPersons = changesetDescriptionTrailers.getTrailers(REPOSITORY, changeset);

    TrailerPersonDto trailerPersonDto = trailerPersons.get(0);

    assertThat(trailerPersonDto.getTrailerType()).isEqualTo("Committed-by");
    assertThat(trailerPersonDto.getName()).isEqualTo(personDto.getName());
    assertThat(trailerPersonDto.getMail()).isEqualTo(personDto.getMail());
  }

  private Changeset createChangeset(String commitMessage) {
    Changeset changeset = new Changeset();
    changeset.setDescription(commitMessage);
    return changeset;
  }

  private DisplayUser createDisplayUser(String name, String mail) {
    return DisplayUser.from(new User(name, name, mail));
  }

  private PersonDto createPersonDto(DisplayUser displayUser) {
    PersonDto personDto = new PersonDto();
    personDto.setName(displayUser.getDisplayName());
    personDto.setMail(displayUser.getMail());
    return personDto;
  }

}
