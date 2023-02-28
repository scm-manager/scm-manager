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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupPermissions;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserPermissions;
import sonia.scm.web.EdisonHalAppender;

import javax.inject.Inject;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class UserToUserDtoMapper extends BaseMapper<User, UserDto> {

  @Inject
  private UserManager userManager;
  @Inject
  private ScmConfiguration scmConfiguration;

  @Override
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "password", ignore = true)
  public abstract UserDto map(User modelObject);

  @Inject
  private ResourceLinks resourceLinks;

  @ObjectFactory
  UserDto createDto(User user) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.user().self(user.getName()));
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.user().delete(user.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.user().update(user.getName())));
      linksBuilder.single(link("publicKeys", resourceLinks.user().publicKeys(user.getName())));
      if (scmConfiguration.isEnabledApiKeys()) {
        linksBuilder.single(link("apiKeys", resourceLinks.user().apiKeys(user.getName())));
      }
      if (user.isExternal()) {
        linksBuilder.single(link("convertToInternal", resourceLinks.user().toInternal(user.getName())));
      } else {
        linksBuilder.single(link("password", resourceLinks.user().passwordChange(user.getName())));
        linksBuilder.single(link("convertToExternal", resourceLinks.user().toExternal(user.getName())));
      }
    }
    if (PermissionPermissions.read().isPermitted()) {
      linksBuilder.single(link("permissions", resourceLinks.userPermissions().permissions(user.getName())));
      if (GroupPermissions.list().isPermitted()) {
        linksBuilder.single(link("permissionOverview", resourceLinks.user().permissionOverview(user.getName())));
      }
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), user);

    return new UserDto(linksBuilder.build(), embeddedBuilder.build());
  }

}
