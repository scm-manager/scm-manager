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

import com.aragost.javahg.commands.PullCommand;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.TagCommandBuilder;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HgTagsCommandTest extends AbstractHgCommandTestBase {

  private SimpleHgWorkingCopyFactory workingCopyFactory;

  @Before
  public void initWorkingCopyFactory() {

    workingCopyFactory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider())) {
      @Override
      public void configure(PullCommand pullCommand) {
        // we do not want to configure http hooks in this unit test
      }
    };
  }

  @Test
  public void shouldGetTagDatesCorrectly() {
    HgTagsCommand hgTagsCommand = new HgTagsCommand(cmdContext);
    final List<Tag> tags = hgTagsCommand.getTags();
    assertThat(tags).hasSize(1);
    assertThat(tags.get(0).getName()).isEqualTo("tip");
    assertThat(tags.get(0).getDate()).contains(1339586381000L);
  }

  @Test
  public void shouldNotDie() throws IOException {
    HgTagCommand hgTagCommand = new HgTagCommand(cmdContext, workingCopyFactory);
    new TagCommandBuilder(cmdContext.get(), hgTagCommand).create().setRevision("79b6baf49711").setName("newtag").execute();

    HgTagsCommand hgTagsCommand = new HgTagsCommand(cmdContext);
    final List<Tag> tags = hgTagsCommand.getTags();

    assertThat(tags).isNotEmpty();
  }

}
