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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.Tag;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitTagsCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Test
  public void shouldGetDatesCorrectly() throws IOException {
    final GitContext gitContext = createContext();
    final GitTagsCommand tagsCommand = new GitTagsCommand(gitContext);
    final List<Tag> tags = tagsCommand.getTags();
    assertThat(tags).hasSize(2);

    Tag annotatedTag = tags.get(0);
    assertThat(annotatedTag.getName()).isEqualTo("1.0.0");
    assertThat(annotatedTag.getDate()).contains(1598348105000L); // Annotated - Take tag date
    assertThat(annotatedTag.getRevision()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    Tag lightweightTag = tags.get(1);
    assertThat(lightweightTag.getName()).isEqualTo("test-tag");
    assertThat(lightweightTag.getDate()).contains(1339416344000L); // Lightweight - Take commit date
    assertThat(lightweightTag.getRevision()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-test-tags.zip";
  }
}
