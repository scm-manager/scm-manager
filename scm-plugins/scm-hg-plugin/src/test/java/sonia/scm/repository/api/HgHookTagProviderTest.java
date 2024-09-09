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
