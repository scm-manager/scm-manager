package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Branch;
import sonia.scm.repository.NamespaceAndName;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BranchToBranchDtoMapperTest {

  private final URI baseUri = URI.create("https://hitchhiker.com");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private BranchToBranchDtoMapperImpl mapper;

  @Test
  void shouldAppendLinks() {
    HalEnricherRegistry registry = new HalEnricherRegistry();
    registry.register(Branch.class, (ctx, appender) -> {
      NamespaceAndName namespaceAndName = ctx.oneRequireByType(NamespaceAndName.class);
      Branch branch = ctx.oneRequireByType(Branch.class);

      appender.appendLink("ka", "http://" + namespaceAndName.logString() + "/" + branch.getName());
    });
    mapper.setRegistry(registry);

    Branch branch = Branch.normalBranch("master", "42");

    BranchDto dto = mapper.map(branch, new NamespaceAndName("hitchhiker", "heart-of-gold"));
    assertThat(dto.getLinks().getLinkBy("ka").get().getHref()).isEqualTo("http://hitchhiker/heart-of-gold/master");
  }

}
