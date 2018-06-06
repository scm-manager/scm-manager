package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;

import java.util.EnumSet;
import java.util.List;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;

public class UserCollectionDto extends HalRepresentation {
  public UserCollectionDto(String baseUrl, NumberedPaging page, List<UserDto> users) {
    super(
      linkingTo()
        .with(page.links(
          fromTemplate(baseUrl + "{?page,pageSize}"),
          EnumSet.allOf(PagingRel.class)))
        .build(),
      embeddedBuilder()
        .with("users", users)
        .build()
    );
  }
}
