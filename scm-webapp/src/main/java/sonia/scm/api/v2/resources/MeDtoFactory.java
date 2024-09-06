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

import com.google.common.base.Strings;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupCollector;
import sonia.scm.user.EMail;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;
import sonia.scm.web.EdisonHalAppender;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

public class MeDtoFactory extends HalAppenderMapper {

  private final ResourceLinks resourceLinks;
  private final GroupCollector groupCollector;
  private final ScmConfiguration scmConfiguration;
  private final EMail eMail;

  @Inject
  public MeDtoFactory(ResourceLinks resourceLinks, GroupCollector groupCollector, ScmConfiguration scmConfiguration, EMail eMail) {
    this.resourceLinks = resourceLinks;
    this.groupCollector = groupCollector;
    this.scmConfiguration = scmConfiguration;
    this.eMail = eMail;
  }

  public MeDto create() {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);

    MeDto dto = createDto(user);
    mapUserProperties(user, dto);
    mapGroups(user, dto);
    setGeneratedMail(user, dto);
    return dto;
  }

  private void mapGroups(User user, MeDto dto) {
    dto.setGroups(groupCollector.collect(user.getName()));
  }

  private void mapUserProperties(User user, MeDto dto) {
    dto.setName(user.getName());
    dto.setDisplayName(user.getDisplayName());
    dto.setMail(user.getMail());
  }

  private void setGeneratedMail(User user, MeDto dto) {
    if (Strings.isNullOrEmpty(user.getMail())) {
      dto.setFallbackMail(eMail.getMailOrFallback(user));
    }
  }

  private MeDto createDto(User user) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.me().self());

    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.me().delete(user.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.me().update(user.getName())));
    }
    if (UserPermissions.changePublicKeys(user).isPermitted()) {
      linksBuilder.single(link("publicKeys", resourceLinks.user().publicKeys(user.getName())));
    }
    if (!user.isExternal() && UserPermissions.changePassword(user).isPermitted()) {
      linksBuilder.single(link("password", resourceLinks.me().passwordChange()));
    }
    if (scmConfiguration.isEnabledApiKeys() && UserPermissions.changeApiKeys(user).isPermitted()) {
      linksBuilder.single(link("apiKeys", resourceLinks.apiKeyCollection().self(user.getName())));
    }

    linksBuilder.single(link("notifications", resourceLinks.me().notifications()));

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), new Me(), user);

    return new MeDto(linksBuilder.build(), embeddedBuilder.build());
  }
}
