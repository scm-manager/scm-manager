package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HgConfigInstallationsToDtoMapperTest {


  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private HgConfigInstallationsToDtoMapper mapper;

  private URI expectedBaseUri;

  private String expectedPath = "path";

  @Before
  public void init() {
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(HgConfigResource.HG_CONFIG_PATH_V2 + "/installations/" + expectedPath);
  }

  @Test
  public void shouldMapFields() {
    List<String> installations = Arrays.asList("/hg", "/bin/hg");

    HgConfigInstallationsDto dto = mapper.map(installations, expectedPath);

    assertThat(dto.getPaths()).isEqualTo(installations);

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
  }
}
