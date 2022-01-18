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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.Manager;
import sonia.scm.api.rest.resources.Simple;

import java.util.Comparator;
import java.util.function.Predicate;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectionResourceManagerAdapterTest {

  @Mock
  private Manager<Simple> manager;
  @Captor
  private ArgumentCaptor<Comparator<Simple>> comparatorCaptor;
  @Captor
  private ArgumentCaptor<Predicate<Simple>> filterCaptor;

  private CollectionResourceManagerAdapter<Simple, HalRepresentation> abstractManagerResource;

  @Before
  public void captureComparator() {
    when(manager.getPage(filterCaptor.capture(), comparatorCaptor.capture(), eq(0), eq(1))).thenReturn(null);
    abstractManagerResource = new SimpleManagerResource();
  }

  @Test
  public void shouldNotSortByDefault() {
    abstractManagerResource.getAll(0, 1, x -> true, null, true, r -> null);

    Comparator<Simple> comparator = comparatorCaptor.getValue();
    assertNull(comparator);
  }

  @Test
  public void shouldAcceptValidSortByParameter() {
    abstractManagerResource.getAll(0, 1, x -> true, "data", true, r -> null);

    Comparator<Simple> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(new Simple("", "1"), new Simple("", "2")) > 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailForIllegalSortByParameter() {
    abstractManagerResource.getAll(0, 1, x -> true, "x", true, r -> null);
  }

  private class SimpleManagerResource extends CollectionResourceManagerAdapter<Simple, HalRepresentation> {
    private SimpleManagerResource() {
      super(CollectionResourceManagerAdapterTest.this.manager, Simple.class);
    }
  }
}
