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
