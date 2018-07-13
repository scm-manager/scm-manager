package sonia.scm.api.rest.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.Manager;
import sonia.scm.ModelObject;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import java.util.Collection;
import java.util.Comparator;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractManagerResourceTest {

  @Mock
  private Manager<Simple, Exception> manager;
  @Mock
  private Request request;
  @Captor
  private ArgumentCaptor<Comparator<Simple>> comparatorCaptor;

  private AbstractManagerResource<Simple, Exception> abstractManagerResource;

  @Before
  public void captureComparator() {
    when(manager.getAll(comparatorCaptor.capture(), eq(0), eq(1))).thenReturn(emptyList());
    abstractManagerResource = new SimpleManagerResource();
  }

  @Test
  public void shouldAcceptDefaultSortByParameter() {
    abstractManagerResource.getAll(request, 0, 1, null, true);

    Comparator<Simple> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(new Simple("1", null), new Simple("2", null)) > 0);
  }

  @Test
  public void shouldAcceptValidSortByParameter() {
    abstractManagerResource.getAll(request, 0, 1, "data", true);

    Comparator<Simple> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(new Simple("", "1"), new Simple("", "2")) > 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailForIllegalSortByParameter() {
    abstractManagerResource.getAll(request, 0, 1, "x", true);
  }


  private class SimpleManagerResource extends AbstractManagerResource<Simple, Exception> {

    {
      disableCache = true;
    }

    private SimpleManagerResource() {
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

    Simple(String id, String data) {
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
    public void setLastModified(Long timestamp) {

    }

    @Override
    public Long getCreationDate() {
      return null;
    }

    @Override
    public void setCreationDate(Long timestamp) {

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
