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

package sonia.scm.security.gpg;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.user.UserPermissions;

import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Links.linkingTo;

public class PublicKeyCollectionMapper {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final PublicKeyMapper mapper;

  @Inject
  public PublicKeyCollectionMapper(Provider<ScmPathInfoStore> scmPathInfoStore, PublicKeyMapper mapper) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.mapper = mapper;
  }

  HalRepresentation map(String username, List<RawGpgKey> keys) {
    List<RawGpgKeyDto> dtos = keys.stream()
      .map(mapper::map)
      .collect(Collectors.toList());

    Links.Builder builder = linkingTo();

    builder.self(selfLink(username));

    if (hasCreatePermissions(username)) {
      builder.single(Link.link("create", createLink(username)));
    }

    return new HalRepresentation(builder.build(), Embedded.embedded("keys", dtos));
  }

  private boolean hasCreatePermissions(String username) {
    return UserPermissions.changePublicKeys(username).isPermitted();
  }

  private String createLink(String username) {
    return new LinkBuilder(scmPathInfoStore.get().get(), UserPublicKeyResource.class)
      .method("create")
      .parameters(username)
      .href();
  }

  private String selfLink(String username) {
    return new LinkBuilder(scmPathInfoStore.get().get(), UserPublicKeyResource.class)
      .method("findAll")
      .parameters(username)
      .href();
  }
}
