package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.installer.HgPackage;

import java.net.URI;

import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.HgConfigTests.assertEqualsPackage;
import static sonia.scm.api.v2.resources.HgConfigTests.createPackage;

@RunWith(MockitoJUnitRunner.class)
public class HgConfigPackageToDtoMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfoStore uriInfoStore;

  @InjectMocks
  private HgConfigPackageToDtoMapperImpl mapper;

  @Before
  public void init() {
    when(uriInfoStore.get().getBaseUri()).thenReturn(baseUri);
  }

  @Test
  public void shouldMapFields() {
    HgPackage hgPackage = createPackage();

    HgConfigPackageDto dto = mapper.map(hgPackage);

    assertEqualsPackage(dto);
  }

}
