package sonia.scm.api.v2.resources;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryType;

import java.net.URI;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryTypeToRepositoryTypeDtoMapperTest {

  private final URI baseUri = URI.create("https://scm-manager.org/scm/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryTypeToRepositoryTypeDtoMapperImpl mapper;

  private RepositoryType type = new RepositoryType("hk", "Hitchhiker", Sets.newHashSet());

  @Test
  public void shouldMapSimpleProperties() {
    RepositoryTypeDto dto = mapper.map(type);
    assertEquals("hk", dto.getName());
    assertEquals("Hitchhiker", dto.getDisplayName());
  }

  @Test
  public void shouldAppendSelfLink() {
    RepositoryTypeDto dto = mapper.map(type);
    assertEquals(
      "https://scm-manager.org/scm/v2/repository-types/hk",
      dto.getLinks().getLinkBy("self").get().getHref()
    );
  }
}
