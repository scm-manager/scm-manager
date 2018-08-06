package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.installer.HgPackage;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.HgConfigPackageCollectionToDtoMapper.COLLECTION_NAME;
import static sonia.scm.api.v2.resources.HgConfigTests.assertEqualsPackage;
import static sonia.scm.api.v2.resources.HgConfigTests.createPackage;

@RunWith(MockitoJUnitRunner.class)
public class HgConfigPackageCollectionToDtoMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfoStore uriInfoStore;

  @InjectMocks
  private HgConfigPackageToDtoMapperImpl hgConfigPackageToDtoMapper;

  private HgConfigPackageCollectionToDtoMapper mapper;

  private URI expectedBaseUri;

  @Before
  public void init() {
    when(uriInfoStore.get().getBaseUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(HgConfigResource.HG_CONFIG_PATH_V2 + "/packages");
    mapper = new HgConfigPackageCollectionToDtoMapper(hgConfigPackageToDtoMapper, uriInfoStore);
  }

  @Test
  public void shouldMapFields() {
    Collection<HgPackage> hgPackages = createPackages();

    HalRepresentation dto = mapper.map(hgPackages);

    List<HalRepresentation> itemsBy = dto.getEmbedded().getItemsBy(COLLECTION_NAME);
    assertThat(itemsBy).hasSize(2);

    HgConfigPackageDto hgPackageDto1 = assertAndGetAsDto(itemsBy.get(0));
    assertEqualsPackage(hgPackageDto1);

    HgConfigPackageDto hgPackageDto2 = assertAndGetAsDto(itemsBy.get(1));
    assertTrue(hgPackageDto2.getLinks().isEmpty());
    // Just verify a random field
    assertThat(hgPackageDto2.getId()).isNull();

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
  }

  private HgConfigPackageDto assertAndGetAsDto(HalRepresentation halRepresentation) {
    assertThat(halRepresentation).isInstanceOf(HgConfigPackageDto.class);
    return (HgConfigPackageDto) halRepresentation;
  }

  private Collection<HgPackage> createPackages() {
    return Arrays.asList(createPackage(), new HgPackage());
  }

}
