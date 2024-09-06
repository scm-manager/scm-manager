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
import com.google.common.collect.Lists;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ConfigValue;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupPermissions;
import sonia.scm.initialization.InitializationFinisher;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.security.AnonymousMode;
import sonia.scm.security.Authentications;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.UserPermissions;
import sonia.scm.web.EdisonHalAppender;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;

public class IndexDtoGenerator extends HalAppenderMapper {

  private final ResourceLinks resourceLinks;
  private final SCMContextProvider scmContextProvider;
  private final ScmConfiguration configuration;
  private final InitializationFinisher initializationFinisher;
  private final SearchEngine searchEngine;
  private final boolean disableFeedback;

  @Inject
  public IndexDtoGenerator(ResourceLinks resourceLinks,
                           SCMContextProvider scmContextProvider,
                           ScmConfiguration configuration,
                           InitializationFinisher initializationFinisher,
                           SearchEngine searchEngine,
                           @ConfigValue(key = "disableFeedback", defaultValue = "false", description = "Disable feedback links in frontend page footer") Boolean disableFeedback) {
    this.resourceLinks = resourceLinks;
    this.scmContextProvider = scmContextProvider;
    this.configuration = configuration;
    this.initializationFinisher = initializationFinisher;
    this.searchEngine = searchEngine;
    this.disableFeedback = disableFeedback;
  }

  public IndexDto generate() {
    return generate(Locale.getDefault());
  }

  public IndexDto generate(Locale locale) {
    Links.Builder builder = Links.linkingTo();
    Embedded.Builder embeddedBuilder = embeddedBuilder();

    builder.self(resourceLinks.index().self());
    builder.single(link("uiPlugins", resourceLinks.uiPluginCollection().self()));

    embeddedBuilder.with("feedback", new FeedbackDto(disableFeedback));

    if (initializationFinisher.isFullyInitialized()) {
      return handleNormalIndex(builder, embeddedBuilder);
    } else {
      return handleInitialization(builder, embeddedBuilder, locale);
    }
  }

  private IndexDto handleNormalIndex(Links.Builder builder, Embedded.Builder embeddedBuilder) {
    List<Link> autoCompleteLinks = Lists.newArrayList();
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
        builder.single(link("pluginCenterAuth", resourceLinks.pluginCenterAuth().auth()));
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
      autoCompleteLinks.add(Link.linkBuilder("autocomplete", resourceLinks.autoComplete().namespaces()).withName("namespaces").build());
      builder.array(autoCompleteLinks);
      if (GroupPermissions.list().isPermitted()) {
        builder.single(link("groups", resourceLinks.groupCollection().self()));
      }
      if (ConfigurationPermissions.list().isPermitted()) {
        builder.single(link("config", resourceLinks.config().self()));
        if (ConfigurationPermissions.write(configuration.getId()).isPermitted()) {
          builder.single(link("invalidateCaches", resourceLinks.invalidationLinks().caches()));
          builder.single(link("invalidateSearchIndex", resourceLinks.invalidationLinks().searchIndex()));
        }
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

      builder.array(searchLinks());
      builder.single(link("searchableTypes", resourceLinks.searchableTypes().searchableTypes()));

      if (!Strings.isNullOrEmpty(configuration.getAlertsUrl())) {
        builder.single(link("alerts", resourceLinks.alerts().get()));
      }
    } else {
      builder.single(link("login", resourceLinks.authentication().jsonLogin()));
    }

    applyEnrichers(new EdisonHalAppender(builder, embeddedBuilder), new Index());
    return new IndexDto(builder.build(), embeddedBuilder.build(), scmContextProvider.getVersion(), scmContextProvider.getInstanceId());
  }

  private List<Link> searchLinks() {
    return searchEngine.getSearchableTypes().stream()
      .map(SearchableType::getName)
      .map(typeName ->
        linkBuilder("search", resourceLinks.search().query(typeName)).withName(typeName).build()
      )
      .collect(Collectors.toList());
  }

  private IndexDto handleInitialization(Links.Builder builder, Embedded.Builder embeddedBuilder, Locale locale) {
    Links.Builder initializationLinkBuilder = Links.linkingTo();
    Embedded.Builder initializationEmbeddedBuilder = embeddedBuilder();
    InitializationStep initializationStep = initializationFinisher.missingInitialization();
    initializationFinisher.getResource(initializationStep.name()).setupIndex(initializationLinkBuilder, initializationEmbeddedBuilder, locale);
    embeddedBuilder.with(initializationStep.name(), new InitializationDto(initializationLinkBuilder.build(), initializationEmbeddedBuilder.build()));
    return new IndexDto(builder.build(), embeddedBuilder.build(), scmContextProvider.getVersion(), scmContextProvider.getInstanceId(), initializationStep.name());
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
