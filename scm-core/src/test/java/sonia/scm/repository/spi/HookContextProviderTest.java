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

package sonia.scm.repository.spi;

import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import sonia.scm.repository.api.HookException;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookFeatureIsNotSupportedException;

/**
 * Unit tests for {@link HookContextProvider}.
 * 
 * @author Sebastian Sdorra
 */
public class HookContextProviderTest {

  /**
   * Expected exception rule.
   */
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  
  private final HookContextProvider simpleHookContextProvider = new HookContextProvider() {
    
    @Override
    public Set<HookFeature> getSupportedFeatures() {
      return Collections.emptySet();
    }
    
  };

  /**
   * Tests {@link HookContextProvider#getSupportedFeatures()}.
   */
  @Test
  public void testGetSupportedFeatures() {
    assertThat(simpleHookContextProvider.getSupportedFeatures(), empty());
  }

  /**
   * Tests {@link HookContextProvider#getBranchProvider()}.
   */  
  @Test
  public void testGetBranchProvider(){
    expectNotSupported(HookFeature.BRANCH_PROVIDER);
    simpleHookContextProvider.getBranchProvider();
  }
  
  /**
   * Tests {@link HookContextProvider#getTagProvider()}.
   */
  @Test
  public void testGetTagProvider(){
    expectNotSupported(HookFeature.TAG_PROVIDER);
    simpleHookContextProvider.getTagProvider();
  }
  
  /**
   * Tests {@link HookContextProvider#getChangesetProvider()}.
   */
  @Test
  public void testGetChangesetProvider(){
    expectNotSupported(HookFeature.CHANGESET_PROVIDER);
    simpleHookContextProvider.getChangesetProvider();
  }
  
  /**
   * Tests {@link HookContextProvider#createMessageProvider()}.
   */
  @Test
  public void testCreateMessageProvider(){
    expectNotSupported(HookFeature.MESSAGE_PROVIDER);
    simpleHookContextProvider.createMessageProvider();
  }
  
  /**
   * Tests {@link HookContextProvider#getMessageProvider()}.
   */
  @Test
  public void testGetMessageProvider(){
    expectNotSupported(HookFeature.MESSAGE_PROVIDER);
    simpleHookContextProvider.getMessageProvider();    
  }
  
  /**
   * Tests {@link HookContextProvider#getMessageProvider()} with disconnected client.
   */
  @Test
  public void testGetMessageProviderDisconnected(){
    expectedException.expect(HookException.class);
    expectedException.expectMessage(containsString("message provider"));
    simpleHookContextProvider.handleClientDisconnect();
    simpleHookContextProvider.getMessageProvider();
  }
  
  private void expectNotSupported(HookFeature feature){
    expectedException.expect(HookFeatureIsNotSupportedException.class);
    expectedException.expectMessage(containsString(feature.toString()));    
  }

}