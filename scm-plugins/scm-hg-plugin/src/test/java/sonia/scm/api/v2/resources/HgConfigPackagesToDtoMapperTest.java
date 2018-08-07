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
import sonia.scm.installer.HgPackages;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.HgConfigTests.assertEqualsPackage;
import static sonia.scm.api.v2.resources.HgConfigTests.createPackage;

@RunWith(MockitoJUnitRunner.class)
public class HgConfigPackagesToDtoMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfoStore uriInfoStore;

  @InjectMocks
  private HgConfigPackagesToDtoMapperImpl mapper;

  private URI expectedBaseUri;

  @Before
  public void init() {
    when(uriInfoStore.get().getBaseUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(HgConfigResource.HG_CONFIG_PATH_V2 + "/packages");
  }

  @Test
  public void shouldMapFields() {
    HgPackages hgPackages = new HgPackages();
    hgPackages.setPackages(createPackages());

    HgConfigPackagesDto dto = mapper.map(hgPackages);

    assertThat(dto.getPackages()).hasSize(2);

    HgConfigPackagesDto.HgConfigPackageDto hgPackageDto1 = dto.getPackages().get(0);
    assertEqualsPackage(hgPackageDto1);

    HgConfigPackagesDto.HgConfigPackageDto hgPackageDto2 = dto.getPackages().get(1);
    // Just verify a random field
    assertThat(hgPackageDto2.getId()).isNull();

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
  }


  private List<HgPackage> createPackages() {
    return Arrays.asList(createPackage(), new HgPackage());
  }

}
