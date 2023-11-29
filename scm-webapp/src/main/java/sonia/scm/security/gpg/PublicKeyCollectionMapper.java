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
