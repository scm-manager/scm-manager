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
