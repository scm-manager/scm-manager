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
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;
import sonia.scm.repository.spi.HookContextProvider;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HookContext}.
 *
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
