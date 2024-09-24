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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChangesetDescriptionContributorProviderTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private final ChangesetDescriptionContributorProvider changesetDescriptionContributors = new ChangesetDescriptionContributorProvider();

  @Test
  void shouldReturnEmptyList() {
    Changeset changeset = createChangeset("zaphod beeblebrox");

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    assertThat(contributors).isNullOrEmpty();
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox");
  }

  @Test
  void shouldConvertTrailerWithCoAuthors() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCo-authored-by: Arthur Dent <dent@hitchhiker.org>");
    changeset.setAuthor(person);
    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Contributor contributor = contributors.iterator().next();
    assertThat(contributor.getType()).isEqualTo("Co-authored-by");
    assertThat(contributor.getPerson()).isEqualTo(createPerson("Arthur Dent", "dent@hitchhiker.org"));
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldIgnoreCoAuthorIfEqualToAuthor() {
    Person person = createPerson("Arthur Dent", "dent@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCo-authored-by: Arthur Dent <dent@hitchhiker.org>");
    changeset.setAuthor(person);

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    assertThat(contributors).isEmpty();
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldConvertTrailerWithReviewers() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nReviewed-by: Tricia McMillan <trillian@hitchhiker.org>");

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Contributor contributor = contributors.iterator().next();

    assertThat(contributor.getType()).isEqualTo("Reviewed-by");
    assertThat(contributor.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldConvertTrailerWithSigner() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\n\nSigned-off-by: Tricia McMillan <trillian@hitchhiker.org>");

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Contributor contributor = contributors.iterator().next();

    assertThat(contributor.getType()).isEqualTo("Signed-off-by");
    assertThat(contributor.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n\n");
  }

  @Test
  void shouldConvertTrailerWithCommitter() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCommitted-by: Tricia McMillan <trillian@hitchhiker.org>");

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Contributor contributor = contributors.iterator().next();

    assertThat(contributor.getType()).isEqualTo("Committed-by");
    assertThat(contributor.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldIgnoreDuplicateContributorWithSameTypeAndMail() {
    Person person = createPerson("Tricia McMillan", "trillian@hitchhiker.org");
    Changeset changeset = createChangeset("zaphod beeblebrox\n\nCommitted-by: Tricia McMillan <trillian@hitchhiker.org>\nCommitted-by: Tricia McMillan <trillian@hitchhiker.org>");

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Contributor contributor = contributors.iterator().next();

    assertThat(contributor.getType()).isEqualTo("Committed-by");
    assertThat(contributor.getPerson()).isEqualTo(person);
    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldConvertMixedTrailers() {
    Changeset changeset = createChangeset("zaphod beeblebrox\n\n" +
      "Committed-by: Tricia McMillan <trillian@hitchhiker.org>\n" +
      "Signed-off-by: Artur Dent <dent@hitchhiker.org>");

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Iterator<Contributor> contributorIterator = contributors.iterator();
    Contributor firstContributor = contributorIterator.next();
    Contributor secondContributor = contributorIterator.next();

    assertThat(firstContributor.getType()).isEqualTo("Committed-by");
    assertThat(firstContributor.getPerson())
      .isEqualTo(createPerson("Tricia McMillan", "trillian@hitchhiker.org"));

    assertThat(secondContributor.getType()).isEqualTo("Signed-off-by");
    assertThat(secondContributor.getPerson())
      .isEqualTo(createPerson("Artur Dent", "dent@hitchhiker.org"));

    assertThat(changeset.getDescription()).isEqualTo("zaphod beeblebrox\n\n");
  }

  @Test
  void shouldNotTouchUnknownTrailers() {
    String originalCommitMessage = "zaphod beeblebrox\n\n" +
      "Some-strange-tag: Tricia McMillan <trillian@hitchhiker.org>";
    Changeset changeset = createChangeset(originalCommitMessage);

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    assertThat(contributors).isNullOrEmpty();
    assertThat(changeset.getDescription()).isEqualTo(originalCommitMessage);
  }

  @Test
  void shouldIgnoreKnownTrailersWithIllegalNameFormat() {
    String originalCommitMessage = "zaphod beeblebrox\n\n" +
      "Committed-by: Tricia McMillan";
    Changeset changeset = createChangeset(originalCommitMessage);

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    assertThat(contributors).isNullOrEmpty();
    assertThat(changeset.getDescription()).isEqualTo(originalCommitMessage);
  }

  @Test
  void shouldIgnoreWhitespacesInEmptyLines() {
    String originalCommitMessage = "zaphod beeblebrox\n    \n" +
      "Committed-by: Tricia McMillan <trillian@hitchhiker.org>";
    Changeset changeset = createChangeset(originalCommitMessage);

    changesetDescriptionContributors.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    assertThat(contributors).isNotEmpty();
  }

  @Test
  void shouldProcessChangesetsSeparately() {
    Changeset changeset1 = createChangeset("message one\n\n" +
      "Committed-by: Tricia McMillan <trillian@hitchhiker.org>");
    Changeset changeset2 = createChangeset("message two");

    ChangesetPreProcessor preProcessor = changesetDescriptionContributors.createPreProcessor(REPOSITORY);
    preProcessor.process(changeset1);
    preProcessor.process(changeset2);

    assertThat(changeset1.getDescription()).isEqualTo("message one\n\n");
    assertThat(changeset1.getContributors()).isNotEmpty();

    assertThat(changeset2.getDescription()).isEqualTo("message two");
    assertThat(changeset2.getContributors()).isNullOrEmpty();
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
