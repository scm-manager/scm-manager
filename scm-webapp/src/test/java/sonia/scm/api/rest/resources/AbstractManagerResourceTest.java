package sonia.scm.api.rest.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.Manager;
import sonia.scm.group.Group;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractManagerResourceTest {

  @Mock
  private UriInfo uriInfo;

  @Mock
  private Manager<Group, Exception> manager;

  @Test
  public void testLocation() throws URISyntaxException {
    URI base = new URI("https://scm.scm-manager.org/");

    TestManagerResource resource = new TestManagerResource(manager);
    when(uriInfo.getAbsolutePath()).thenReturn(base);

    URI uri = resource.location(uriInfo, "special-group");
    assertEquals(new URI("https://scm.scm-manager.org/groups/special-group"), uri);
  }

  @Test
  public void testLocationWithSpaces() throws URISyntaxException {
    URI base = new URI("https://scm.scm-manager.org/");

    TestManagerResource resource = new TestManagerResource(manager);
    when(uriInfo.getAbsolutePath()).thenReturn(base);

    URI uri = resource.location(uriInfo, "Scm Special Group");
    assertEquals(new URI("https://scm.scm-manager.org/groups/Scm%20Special%20Group"), uri);
  }

  private static class TestManagerResource extends AbstractManagerResource<Group, Exception> {

    private TestManagerResource(Manager<Group, Exception> manager) {
      super(manager);
    }

    @Override
    protected GenericEntity<Collection<Group>> createGenericEntity(Collection<Group> items) {
      return null;
    }

    @Override
    protected String getId(Group group) {
      return group.getId();
    }

    @Override
    protected String getPathPart() {
      return "groups";
    }
  }
}
