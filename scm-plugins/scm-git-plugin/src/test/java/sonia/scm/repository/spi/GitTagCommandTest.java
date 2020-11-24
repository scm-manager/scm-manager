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

import org.eclipse.jgit.lib.GpgSigner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.security.GPG;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GitTagCommandTest extends AbstractGitCommandTestBase {

  @Mock
  private GPG gpg;

  @Before
  public void setSigner() {
    GpgSigner.setDefault(new GitTestHelper.SimpleGpgSigner());
  }

  @Test
  public void shouldCreateATag() throws IOException {
    createCommand().create(new TagCreateRequest("592d797cd36432e591416e8b2b98154f4f163411", "newtag"));
    Optional<Tag> tag = findTag(createContext(), "newtag");
    assertThat(tag).isNotEmpty();
  }

  @Test
  public void shouldDeleteATag() throws IOException {
    final GitContext context = createContext();
    Optional<Tag> tag = findTag(context, "test-tag");
    assertThat(tag).isNotEmpty();

    createCommand().delete(new TagDeleteRequest("test-tag"));

    tag = findTag(context, "test-tag");
    assertThat(tag).isEmpty();
  }

  private GitTagCommand createCommand() {
    return new GitTagCommand(createContext(), gpg);
  }

  private List<Tag> readTags(GitContext context) throws IOException {
    return new GitTagsCommand(context, gpg).getTags();
  }

  private Optional<Tag> findTag(GitContext context, String name) throws IOException {
    List<Tag> branches = readTags(context);
    return branches.stream().filter(b -> name.equals(b.getName())).findFirst();
  }
}
