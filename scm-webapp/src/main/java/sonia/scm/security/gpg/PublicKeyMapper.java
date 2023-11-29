/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
