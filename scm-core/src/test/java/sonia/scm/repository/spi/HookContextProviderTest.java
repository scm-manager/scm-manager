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
