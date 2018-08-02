package sonia.scm.api.v2.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryType;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryTypeCollectionToDtoMapperTest {

  private final URI baseUri = URI.create("https://scm-manager.org/scm/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryTypeToRepositoryTypeDtoMapperImpl mapper;

  private List<RepositoryType> types = Lists.newArrayList(
    new RepositoryType("hk", "Hitchhiker", Sets.newHashSet()),
    new RepositoryType("hog", "Heart of Gold", Sets.newHashSet())
  );

  private RepositoryTypeCollectionToDtoMapper collectionMapper;

  @Before
  public void setUpEnvironment() {
    collectionMapper = new RepositoryTypeCollectionToDtoMapper(mapper, resourceLinks);
  }

  @Test
  public void shouldHaveEmbeddedDtos() {
    HalRepresentation mappedTypes = collectionMapper.map(types);
    Embedded embedded = mappedTypes.getEmbedded();
    List<RepositoryTypeDto> embeddedTypes = embedded.getItemsBy("repository-types", RepositoryTypeDto.class);
    assertEquals("hk", embeddedTypes.get(0).getName());
    assertEquals("hog", embeddedTypes.get(1).getName());
  }

  @Test
  public void shouldHaveSelfLink() {
    HalRepresentation mappedTypes = collectionMapper.map(types);
    Optional<Link> self = mappedTypes.getLinks().getLinkBy("self");
    assertEquals("https://scm-manager.org/scm/v2/repository-types/", self.get().getHref());
  }

}
