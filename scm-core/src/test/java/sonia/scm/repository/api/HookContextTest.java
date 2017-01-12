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
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;
import sonia.scm.repository.spi.HookContextProvider;

/**
 * Unit tests for {@link HookContext}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class HookContextTest {

  @Mock
  private HookContextProvider provider;
  
  @Mock
  private Repository repository;
  
  @Mock
  private PreProcessorUtil preProcessorUtil;
  
  @Mock
  private HookChangesetProvider changesetProvider;
  
  @InjectMocks
  private HookContext context;
  
  /**
   * Set up mocks for upcoming test.
   */
  @Before
  public void setUpMocks(){
    when(repository.getName()).thenReturn("test");
    when(provider.getChangesetProvider()).thenReturn(changesetProvider);
    when(provider.getSupportedFeatures()).thenReturn(Sets.newHashSet(HookFeature.CHANGESET_PROVIDER));
    
    List<Changeset> changesets = Lists.newArrayList(new Changeset("1", Long.MIN_VALUE, new Person("Trillian")));
    HookChangesetResponse response = new HookChangesetResponse(changesets);
    when(changesetProvider.handleRequest(any(HookChangesetRequest.class))).thenReturn(response);
  }
  
  /**
   * Tests {@link HookContext#getBranchProvider()}.
   */
  @Test
  public void testGetBranchProvider() {
    context.getBranchProvider();
    
    verify(provider).getBranchProvider();
  }

  /**
   * Tests {@link HookContext#getTagProvider()}.
   */
  @Test
  public void testGetTagProvider() {
    context.getTagProvider();
    
    verify(provider).getTagProvider();
  }
  
  /**
   * Tests {@link HookContext#getMessageProvider()}.
   */
  @Test
  public void testGetMessageProvider() {
    context.getMessageProvider();
    
    verify(provider).getMessageProvider();
  }
  
  /**
   * Tests {@link HookContext#getChangesetProvider()}.
   */
  @Test
  public void testGetChangesetProvider() {
    HookChangesetBuilder builder = context.getChangesetProvider();
    List<Changeset> changesets = builder.getChangesetList();
    assertNotNull(changesets);
    assertEquals("1", changesets.get(0).getId());
  }
  
  /**
   * Tests {@link HookContext#isFeatureSupported(sonia.scm.repository.api.HookFeature)}.
   */
  @Test
  public void testIsFeatureSupported(){
    assertTrue(context.isFeatureSupported(HookFeature.CHANGESET_PROVIDER));
    assertFalse(context.isFeatureSupported(HookFeature.BRANCH_PROVIDER));
  }
  
}