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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectionResourceManagerAdapterTest {

  @Mock
  private Manager<Simple> manager;
  @Captor
  private ArgumentCaptor<Comparator<Simple>> comparatorCaptor;

  private CollectionResourceManagerAdapter<Simple, HalRepresentation> abstractManagerResource;

  @Before
  public void captureComparator() {
    when(manager.getPage(comparatorCaptor.capture(), eq(0), eq(1))).thenReturn(null);
    abstractManagerResource = new SimpleManagerResource();
  }

  @Test
  public void shouldAcceptDefaultSortByParameter() {
    abstractManagerResource.getAll(0, 1, null, true, r -> null);

    Comparator<Simple> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(new Simple("1", null), new Simple("2", null)) > 0);
  }

  @Test
  public void shouldAcceptValidSortByParameter() {
    abstractManagerResource.getAll(0, 1, "data", true, r -> null);

    Comparator<Simple> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(new Simple("", "1"), new Simple("", "2")) > 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailForIllegalSortByParameter() {
    abstractManagerResource.getAll(0, 1, "x", true, r -> null);
  }

  private class SimpleManagerResource extends CollectionResourceManagerAdapter<Simple, HalRepresentation> {
    private SimpleManagerResource() {
      super(CollectionResourceManagerAdapterTest.this.manager, Simple.class);
    }
  }
}
