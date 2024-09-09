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
import sonia.scm.repository.Tag;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HgTagsCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldGetTagDatesCorrectly() {
    HgTagsCommand hgTagsCommand = new HgTagsCommand(cmdContext);
    final List<Tag> tags = hgTagsCommand.getTags();
    assertThat(tags).hasSize(1);
    assertThat(tags.get(0).getName()).isEqualTo("tip");
    assertThat(tags.get(0).getDate()).contains(1339586381000L);
  }

}
