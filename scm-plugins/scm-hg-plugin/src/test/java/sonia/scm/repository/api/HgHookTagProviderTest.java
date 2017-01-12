/***
 * Copyright (c) 2015, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * https://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.repository.api;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Tag;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;

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
    assertThat(tagProvider.getDeletedTags(), empty());
  }
  
  /**
   * Tests {@link HgHookTagProvider#getCreatedTags()}.
   */
  @Test
  public void testGetCreatedTags(){
    Changeset c1 = new Changeset("1", Long.MIN_VALUE, null);
    c1.getTags().add("1.0.0");
    Changeset c2 = new Changeset("2", Long.MIN_VALUE, null);
    c2.getTags().add("2.0.0");
    Changeset c3 = new Changeset("3", Long.MIN_VALUE, null);
    prepareChangesets(c1, c2, c3);
    
    List<Tag> tags = tagProvider.getCreatedTags();
    assertNotNull(tags);
    assertEquals(2, tags.size());
    
    Tag t1 = tags.get(0);
    assertEquals("1", t1.getRevision());
    assertEquals("1.0.0", t1.getName());
    
    Tag t2 = tags.get(1);
    assertEquals("2", t2.getRevision());
    assertEquals("2.0.0", t2.getName());
  }

  private void prepareChangesets(Changeset... changesets){
    List<Changeset> list = Lists.newArrayList(changesets);
    HookChangesetResponse response = new HookChangesetResponse(list);
    when(changesetProvider.handleRequest(Mockito.any(HookChangesetRequest.class))).thenReturn(response);
  }
}