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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.user.UserPermissions;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class PublicKeyMapper {

  @Inject
  private Provider<ScmPathInfoStore> scmPathInfoStore;

  @VisibleForTesting
  void setScmPathInfoStore(Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "raw", ignore = true)
  abstract RawGpgKeyDto map(RawGpgKey rawGpgKey);

  @ObjectFactory
  RawGpgKeyDto createDto(RawGpgKey rawGpgKey) {
    Links.Builder linksBuilder = linkingTo();
    linksBuilder.self(createSelfLink(rawGpgKey));
    if (UserPermissions.changePublicKeys(rawGpgKey.getOwner()).isPermitted() && !rawGpgKey.isReadonly()) {
      linksBuilder.single(Link.link("delete", createDeleteLink(rawGpgKey)));
    }
    linksBuilder.single(Link.link("raw", createDownloadLink(rawGpgKey)));
    return new RawGpgKeyDto(linksBuilder.build());
  }

  private String createSelfLink(RawGpgKey rawGpgKey) {
    return new LinkBuilder(scmPathInfoStore.get().get(), UserPublicKeyResource.class)
      .method("findByIdJson")
      .parameters(rawGpgKey.getOwner(), rawGpgKey.getId())
      .href();
  }

  private String createDeleteLink(RawGpgKey rawGpgKey) {
    return new LinkBuilder(scmPathInfoStore.get().get(), UserPublicKeyResource.class)
      .method("deleteById")
      .parameters(rawGpgKey.getOwner(), rawGpgKey.getId())
      .href();
  }

  private String createDownloadLink(RawGpgKey rawGpgKey) {
    return new LinkBuilder(scmPathInfoStore.get().get(), PublicKeyResource.class)
      .method("findByIdGpg")
      .parameters(rawGpgKey.getId())
      .href();
  }
}
