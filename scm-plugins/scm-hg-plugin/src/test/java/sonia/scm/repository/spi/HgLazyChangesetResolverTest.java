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

package sonia.scm.repository.spi;

import org.junit.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.Person;

import static org.assertj.core.api.Assertions.assertThat;

public class HgLazyChangesetResolverTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldResolveChangesets() {
    HgLazyChangesetResolver changesetResolver = new HgLazyChangesetResolver(HgTestUtil.createFactory(handler, repositoryDirectory), cmdContext);
    Iterable<Changeset> changesets = changesetResolver.call();

    Changeset firstChangeset = changesets.iterator().next();
    assertThat(firstChangeset.getId()).isEqualTo("2baab8e80280ef05a9aa76c49c76feca2872afb7");
    assertThat(firstChangeset.getDate()).isEqualTo(1339586381000L);
    assertThat(firstChangeset.getAuthor()).isEqualTo(Person.toPerson("Zaphod Beeblebrox <zaphod.beeblebrox@hitchhiker.com>"));
    assertThat(firstChangeset.getDescription()).isEqualTo("added new line for blame");
  }
}
