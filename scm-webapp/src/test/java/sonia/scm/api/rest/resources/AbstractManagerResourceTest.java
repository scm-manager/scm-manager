package sonia.scm.api.rest.resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import sonia.scm.Manager;
import sonia.scm.ModelObject;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import java.util.Collection;
import java.util.Comparator;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractManagerResourceTest {

  private final Manager<Simple, Exception> manager = mock(Manager.class);
  private final Request request = mock(Request.class);
  private final ArgumentCaptor<Comparator> comparatorCaptor = forClass(Comparator.class);

  private final AbstractManagerResource<Simple, Exception> abstractManagerResource = new SimpleManagerResource();

  @Test
  public void shouldAcceptDefaultSortByParameter() {
    abstractManagerResource.getAll(request, 0, 1, null, true);

    Comparator comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(new Simple("1", null), new Simple("2", null)) > 0);
  }

  @Test
  public void shouldAcceptValidSortByParameter() {
    abstractManagerResource.getAll(request, 0, 1, "data", true);

    Comparator comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(new Simple("", "1"), new Simple("", "2")) > 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailForIllegalSortByParameter() {
    abstractManagerResource.getAll(request, 0, 1, "x", true);
  }

  @Before
  public void captureComparator() {
    when(manager.getAll(comparatorCaptor.capture(), eq(0), eq(1))).thenReturn(emptyList());
  }

  private class SimpleManagerResource extends AbstractManagerResource<Simple, Exception> {

    {
      disableCache = true;
    }

    public SimpleManagerResource() {
      super(AbstractManagerResourceTest.this.manager, Simple.class);
    }

    @Override
    protected GenericEntity<Collection<Simple>> createGenericEntity(Collection<Simple> items) {
      return null;
    }

    @Override
    protected String getId(Simple item) {
      return null;
    }

    @Override
    protected String getPathPart() {
      return null;
    }
  }

  public static class Simple implements ModelObject {

    private String id;
    private String data;

    public Simple(String id, String data) {
      this.id = id;
      this.data = data;
    }

    public String getData() {
      return data;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public Long getLastModified() {
      return null;
    }

    @Override
    public String getType() {
      return null;
    }
    @Override
    public boolean isValid() {
      return false;
    }
  }
}
