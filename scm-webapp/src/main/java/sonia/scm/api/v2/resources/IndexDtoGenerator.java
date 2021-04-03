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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupPermissions;
import sonia.scm.initialization.InitializationFinisher;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.security.AnonymousMode;
import sonia.scm.security.Authentications;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.UserPermissions;
import sonia.scm.web.EdisonHalAppender;

import javax.inject.Inject;
import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;

public class IndexDtoGenerator extends HalAppenderMapper {

  private final ResourceLinks resourceLinks;
  private final SCMContextProvider scmContextProvider;
  private final ScmConfiguration configuration;
  private final InitializationFinisher initializationFinisher;

  @Inject
  public IndexDtoGenerator(ResourceLinks resourceLinks, SCMContextProvider scmContextProvider, ScmConfiguration configuration, InitializationFinisher initializationFinisher) {
    this.resourceLinks = resourceLinks;
    this.scmContextProvider = scmContextProvider;
    this.configuration = configuration;
    this.initializationFinisher = initializationFinisher;
  }

  public IndexDto generate() {
    Links.Builder builder = Links.linkingTo();
    Embedded.Builder embeddedBuilder = embeddedBuilder();

    List<Link> autoCompleteLinks = Lists.newArrayList();
    builder.self(resourceLinks.index().self());
    builder.single(link("uiPlugins", resourceLinks.uiPluginCollection().self()));

    if (initializationFinisher.isFullyInitialized()) {
      String loginInfoUrl = configuration.getLoginInfoUrl();
      if (!Strings.isNullOrEmpty(loginInfoUrl)) {
        builder.single(link("loginInfo", loginInfoUrl));
      }

      if (shouldAppendSubjectRelatedLinks()) {
        builder.single(link("me", resourceLinks.me().self()));

        if (Authentications.isAuthenticatedSubjectAnonymous()) {
          builder.single(link("login", resourceLinks.authentication().jsonLogin()));
        } else {
          builder.single(link("logout", resourceLinks.authentication().logout()));
        }

        if (PluginPermissions.read().isPermitted()) {
          builder.single(link("installedPlugins", resourceLinks.installedPluginCollection().self()));
          builder.single(link("availablePlugins", resourceLinks.availablePluginCollection().self()));
        }
        if (PluginPermissions.write().isPermitted()) {
          builder.single(link("pendingPlugins", resourceLinks.pendingPluginCollection().self()));
        }
        if (UserPermissions.list().isPermitted()) {
          builder.single(link("users", resourceLinks.userCollection().self()));
        }
        if (UserPermissions.autocomplete().isPermitted()) {
          autoCompleteLinks.add(Link.linkBuilder("autocomplete", resourceLinks.autoComplete().users()).withName("users").build());
        }
        if (GroupPermissions.autocomplete().isPermitted()) {
          autoCompleteLinks.add(Link.linkBuilder("autocomplete", resourceLinks.autoComplete().groups()).withName("groups").build());
        }
        builder.array(autoCompleteLinks);
        if (GroupPermissions.list().isPermitted()) {
          builder.single(link("groups", resourceLinks.groupCollection().self()));
        }
        if (ConfigurationPermissions.list().isPermitted()) {
          builder.single(link("config", resourceLinks.config().self()));
          if (!Strings.isNullOrEmpty(configuration.getReleaseFeedUrl())) {
            builder.single(link("updateInfo", resourceLinks.adminInfo().updateInfo()));
          }
        }
        builder.single(link("repositories", resourceLinks.repositoryCollection().self()));
        builder.single(link("namespaces", resourceLinks.namespaceCollection().self()));
        if (PermissionPermissions.list().isPermitted()) {
          builder.single(link("permissions", resourceLinks.permissions().self()));
        }
        builder.single(link("repositoryVerbs", resourceLinks.repositoryVerbs().self()));

        builder.single(link("repositoryTypes", resourceLinks.repositoryTypeCollection().self()));
        builder.single(link("namespaceStrategies", resourceLinks.namespaceStrategies().self()));
        builder.single(link("repositoryRoles", resourceLinks.repositoryRoleCollection().self()));
        builder.single(link("importLog", resourceLinks.repository().importLog("IMPORT_LOG_ID").replace("IMPORT_LOG_ID", "{logId}")));
      } else {
        builder.single(link("login", resourceLinks.authentication().jsonLogin()));
      }

      applyEnrichers(new EdisonHalAppender(builder, embeddedBuilder), new Index());
      return new IndexDto(builder.build(), embeddedBuilder.build(), scmContextProvider.getVersion());
    } else {
      Links.Builder initializationLinkBuilder = Links.linkingTo();
      Embedded.Builder initializationEmbeddedBuilder = embeddedBuilder();
      InitializationStep initializationStep = initializationFinisher.missingInitialization();
      initializationStep.setupIndex(initializationLinkBuilder, initializationEmbeddedBuilder);
      embeddedBuilder.with(initializationStep.name(), new InitializationDto(initializationLinkBuilder.build(), initializationEmbeddedBuilder.build()));
      return new IndexDto(builder.build(), embeddedBuilder.build(), scmContextProvider.getVersion(), initializationStep.name());
    }
  }

  private boolean shouldAppendSubjectRelatedLinks() {
    return isAuthenticatedSubjectNotAnonymous()
      || isAuthenticatedSubjectAllowedToBeAnonymous();
  }

  private boolean isAuthenticatedSubjectAllowedToBeAnonymous() {
    return Authentications.isAuthenticatedSubjectAnonymous()
      && configuration.getAnonymousMode() == AnonymousMode.FULL;
  }

  private boolean isAuthenticatedSubjectNotAnonymous() {
    return SecurityUtils.getSubject().isAuthenticated()
      && !Authentications.isAuthenticatedSubjectAnonymous();
  }
}
