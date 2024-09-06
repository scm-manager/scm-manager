/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.admin.UpdateInfo;
import sonia.scm.api.v2.resources.UpdateInfoDto;
import sonia.scm.api.v2.resources.UpdateInfoMapper;
import sonia.scm.api.v2.resources.UpdateInfoMapperImpl;
import sonia.scm.api.v2.resources.ResourceLinks;
import sonia.scm.api.v2.resources.ResourceLinksMock;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UpdateInfoMapperTest {

  private final URI baseUri = URI.create("https://hitchhiker.com/scm/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private UpdateInfoMapperImpl mapper;

  @Test
  void shouldMapToDto() {
    UpdateInfo updateInfo = new UpdateInfo("1.2.3", "download-link");
    UpdateInfoDto dto = mapper.map(updateInfo);

    assertThat(dto.getLink()).isEqualTo(updateInfo.getLink());
    assertThat(dto.getLatestVersion()).isEqualTo(updateInfo.getLatestVersion());
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("https://hitchhiker.com/scm/v2/updateInfo");
  }
}
