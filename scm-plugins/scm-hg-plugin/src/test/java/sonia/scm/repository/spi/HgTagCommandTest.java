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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.javahg.commands.PullCommand;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HgTagCommandTest extends AbstractHgCommandTestBase {

  private SimpleHgWorkingCopyFactory workingCopyFactory;

  @Before
  public void initWorkingCopyFactory() {

    workingCopyFactory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(null, repositoryLocationResolver)), new SimpleMeterRegistry()) {
      @Override
      public void configure(PullCommand pullCommand) {
        // we do not want to configure http hooks in this unit test
      }
    };
  }

  @Test
  public void shouldCreateAndDeleteTagCorrectly() {
    // Create
    new HgTagCommand(cmdContext, workingCopyFactory).create(new TagCreateRequest("79b6baf49711", "newtag"));
    List<Tag> tags = new HgTagsCommand(cmdContext).getTags();
    assertThat(tags).hasSize(2);
    final Tag newTag = tags.get(1);
    assertThat(newTag.getName()).isEqualTo("newtag");

    // Delete
    new HgTagCommand(cmdContext, workingCopyFactory).delete(new TagDeleteRequest("newtag"));
    tags = new HgTagsCommand(cmdContext).getTags();
    assertThat(tags).hasSize(1);
  }

}
