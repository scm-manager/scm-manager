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

package sonia.scm.repository.api;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Tag;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HgHookTagProvider}.
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class HgHookTagProviderTest {

  @Mock
  private HookChangesetProvider changesetProvider;

  @InjectMocks
  private HgHookTagProvider tagProvider;

  /**
   * Tests {@link HgHookTagProvider#getDeletedTags()}.
   */
  @Test
  public void testGetDeletedTags() {
    prepareChangesets(new Changeset("1", Long.MIN_VALUE, null));
    assertThat(tagProvider.getDeletedTags()).isEmpty();
  }

  /**
   * Tests {@link HgHookTagProvider#getCreatedTags()}.
   */
  @Test
  public void testGetCreatedTags(){
    Changeset c1 = new Changeset("1", Long.MIN_VALUE, null);
    c1.getTags().add("1.0.0");
    Changeset c2 = new Changeset("2", Long.MAX_VALUE, null);
    c2.getTags().add("2.0.0");
    Changeset c3 = new Changeset("3", Long.MIN_VALUE, null);
    prepareChangesets(c1, c2, c3);

    List<Tag> tags = tagProvider.getCreatedTags();
    assertNotNull(tags);
    assertEquals(2, tags.size());

    Tag t1 = tags.get(0);
    assertEquals("1", t1.getRevision());
    assertEquals("1.0.0", t1.getName());
    assertThat(t1.getDate()).contains(Long.MIN_VALUE);

    Tag t2 = tags.get(1);
    assertEquals("2", t2.getRevision());
    assertEquals("2.0.0", t2.getName());
    assertThat(t2.getDate()).contains(Long.MAX_VALUE);
  }

  private void prepareChangesets(Changeset... changesets){
    List<Changeset> list = Lists.newArrayList(changesets);
    HookChangesetResponse response = new HookChangesetResponse(list);
    when(changesetProvider.handleRequest(Mockito.any(HookChangesetRequest.class))).thenReturn(response);
  }
}
