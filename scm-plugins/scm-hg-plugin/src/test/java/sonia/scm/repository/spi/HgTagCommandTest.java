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
