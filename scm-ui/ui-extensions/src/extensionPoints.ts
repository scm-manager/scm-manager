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

import React from "react";
import {
  Branch,
  BranchDetails,
  Changeset,
  File,
  Group,
  HalRepresentation,
  IndexResources,
  Links,
  Me,
  NamespaceStrategies,
  Plugin,
  Repository,
  RepositoryCreation,
  RepositoryRole,
  RepositoryRoleBase,
  RepositoryTypeCollection,
  User,
} from "@scm-manager/ui-types";
import { ExtensionPointDefinition } from "./binder";
import { RenderableExtensionPointDefinition, SimpleRenderableDynamicExtensionPointDefinition } from "./ExtensionPoint";
import { Replacement } from "@scm-manager/ui-components";

type ExtractProps<T> = T extends React.ComponentType<infer U> ? U : never;

type RepositoryCreatorSubFormProps = {
  repository: RepositoryCreation;
  onChange: (repository: RepositoryCreation) => void;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

export type RepositoryCreatorComponentProps = ExtractProps<RepositoryCreator["type"]["component"]>;

/**
 * @deprecated use {@link RepositoryCreator}`["type"]` instead
 */
export type RepositoryCreatorExtension = RepositoryCreator["type"];
export type RepositoryCreator = ExtensionPointDefinition<
  "repos.creator",
  {
    subtitle: string;
    path: string;
    icon: string;
    label: string;
    component: React.ComponentType<{
      namespaceStrategies: NamespaceStrategies;
      repositoryTypes: RepositoryTypeCollection;
      index: IndexResources;

      nameForm: React.ComponentType<RepositoryCreatorSubFormProps>;
      informationForm: React.ComponentType<RepositoryCreatorSubFormProps>;
    }>;
  }
>;

export type RepositoryFlags = ExtensionPointDefinition<"repository.flags", { repository: Repository }>;

/**
 * @deprecated use {@link ReposSourcesActionbar}`["props"]` instead
 */
export type ReposSourcesActionbarExtensionProps = ReposSourcesActionbar["props"];
/**
 * @deprecated use {@link ReposSourcesActionbar} instead
 */
export type ReposSourcesActionbarExtension = React.ComponentType<ReposSourcesActionbarExtensionProps>;
export type ReposSourcesActionbar = ExtensionPointDefinition<
  "repos.sources.actionbar",
  {
    baseUrl: string;
    revision: string;
    branch: Branch | undefined;
    path: string;
    sources: File;
    repository: Repository;
  }
>;

/**
 * @deprecated use {@link ReposSourcesEmptyActionbar}`["props"]` instead
 */
export type ReposSourcesEmptyActionbarExtensionProps = ReposSourcesEmptyActionbar["props"];
/**
 * @deprecated use {@link ReposSourcesEmptyActionbar} instead
 */
export type ReposSourcesEmptyActionbarExtension = ReposSourcesEmptyActionbar;
export type ReposSourcesEmptyActionbar = ExtensionPointDefinition<
  "repos.sources.empty.actionbar",
  {
    sources: File;
    repository: Repository;
  }
>;

/**
 * @deprecated use {@link ReposSourcesTreeWrapper}`["props"]` instead
 */
export type ReposSourcesTreeWrapperProps = ReposSourcesTreeWrapper["props"];

/**
 * @deprecated use {@link ReposSourcesTreeWrapper} instead
 */
export type ReposSourcesTreeWrapperExtension = ReposSourcesTreeWrapper;
export type ReposSourcesTreeWrapper = RenderableExtensionPointDefinition<
  "repos.source.tree.wrapper",
  {
    repository: Repository;
    directory: File;
    baseUrl: string;
    revision: string;
  }
>;

export type ReposSourcesTreeRowProps = {
  repository: Repository;
  file: File;
};

/**
 * @deprecated use {@link ReposSourcesTreeRowRight} instead
 */
export type ReposSourcesTreeRowRightExtension = ReposSourcesTreeRowRight;
export type ReposSourcesTreeRowRight = RenderableExtensionPointDefinition<
  "repos.sources.tree.row.right",
  ReposSourcesTreeRowProps
>;

/**
 * @deprecated use {@link ReposSourcesTreeRowAfter} instead
 */
export type ReposSourcesTreeRowAfterExtension = ReposSourcesTreeRowAfter;
export type ReposSourcesTreeRowAfter = RenderableExtensionPointDefinition<
  "repos.sources.tree.row.after",
  ReposSourcesTreeRowProps
>;

/**
 * @deprecated use {@link PrimaryNavigationLoginButton}`["props"]` instead
 */
export type PrimaryNavigationLoginButtonProps = PrimaryNavigationLoginButton["props"];

/**
 * use {@link PrimaryNavigationLoginButton} instead
 */
export type PrimaryNavigationLoginButtonExtension = PrimaryNavigationLoginButton;
export type PrimaryNavigationLoginButton = RenderableExtensionPointDefinition<
  "primary-navigation.login",
  {
    links: Links;
    label: string;
    loginUrl: string;
    from: string;
    to: string;
    className: string;
    content: React.ReactNode;
  }
>;

/**
 * @deprecated use {@link PrimaryNavigationLogoutButtonExtension}`["props"]` instead
 */
export type PrimaryNavigationLogoutButtonProps = PrimaryNavigationLogoutButton["props"];

/**
 * @deprecated use {@link PrimaryNavigationLogoutButton} instead
 */
export type PrimaryNavigationLogoutButtonExtension = PrimaryNavigationLogoutButton;
export type PrimaryNavigationLogoutButton = RenderableExtensionPointDefinition<
  "primary-navigation.logout",
  {
    links: Links;
    label: string;
    className: string;
    content: React.ReactNode;
  }
>;

/**
 * @deprecated use {@link SourceExtension}`["props"]` instead
 */
export type SourceExtensionProps = SourceExtension["props"];
export type SourceExtension = RenderableExtensionPointDefinition<
  "repos.sources.extensions",
  {
    repository: Repository;
    baseUrl: string;
    revision: string;
    extension: string;
    sources: File | undefined;
    path: string;
  }
>;

/**
 * @deprecated use {@link RepositoryOverviewTop}`["props"]` instead
 */
export type RepositoryOverviewTopExtensionProps = RepositoryOverviewTop["props"];

/**
 * @deprecated use {@link RepositoryOverviewTop} instead
 */
export type RepositoryOverviewTopExtension = RepositoryOverviewTop;
export type RepositoryOverviewTop = RenderableExtensionPointDefinition<
  "repository.overview.top",
  {
    page: number;
    search: string;
    namespace?: string;
  }
>;

/**
 * @deprecated use {@link RepositoryOverviewLeft} instead
 */
export type RepositoryOverviewLeftExtension = RepositoryOverviewLeft;
export type RepositoryOverviewLeft = ExtensionPointDefinition<"repository.overview.left", React.ComponentType>;

/**
 * @deprecated use {@link RepositoryOverviewTitle} instead
 */
export type RepositoryOverviewTitleExtension = RepositoryOverviewTitle;
export type RepositoryOverviewTitle = RenderableExtensionPointDefinition<"repository.overview.title">;

/**
 * @deprecated use {@link RepositoryOverviewSubtitle} instead
 */
export type RepositoryOverviewSubtitleExtension = RepositoryOverviewSubtitle;
export type RepositoryOverviewSubtitle = RenderableExtensionPointDefinition<"repository.overview.subtitle">;

// From docs

export type AdminNavigation = RenderableExtensionPointDefinition<"admin.navigation", { links: Links; url: string }>;

export type AdminRoute = RenderableExtensionPointDefinition<"admin.route", { links: Links; url: string }>;

export type AdminSetting = RenderableExtensionPointDefinition<"admin.setting", { links: Links; url: string }>;

/**
 * - can be used to replace the whole description of a changeset
 *
 * @deprecated Use `changeset.description.tokens` instead
 */
export type ChangesetDescription = RenderableExtensionPointDefinition<
  "changeset.description",
  { changeset: Changeset; value: string }
>;

/**
 * - Can be used to replace parts of a changeset description with components
 * - Has to be bound with a funktion taking the changeset and the (partial) description and returning `Replacement` objects with the following attributes:
 * - textToReplace: The text part of the description that should be replaced by a component
 * - replacement: The component to take instead of the text to replace
 * - replaceAll: Optional boolean; if set to `true`, all occurances of the text will be replaced (default: `false`)
 */
export type ChangesetDescriptionTokens = ExtensionPointDefinition<
  "changeset.description.tokens",
  (changeset: Changeset, value: string) => Replacement[],
  { changeset: Changeset; value: string }
>;

export type ChangesetRight = RenderableExtensionPointDefinition<
  "changeset.right",
  { repository: Repository; changeset: Changeset }
>;

export type ChangesetsAuthorSuffix = RenderableExtensionPointDefinition<
  "changesets.author.suffix",
  { changeset: Changeset }
>;

export type GroupNavigation = RenderableExtensionPointDefinition<"group.navigation", { group: Group; url: string }>;

export type GroupRoute = RenderableExtensionPointDefinition<"group.route", { group: Group; url: string }>;
export type GroupSetting = RenderableExtensionPointDefinition<"group.setting", { group: Group; url: string }>;

/**
 * - Add a new Route to the main Route (scm/)
 * - Props: authenticated?: boolean, links: Links
 */
export type MainRoute = RenderableExtensionPointDefinition<
  "main.route",
  {
    me: Me;
    authenticated?: boolean;
  }
>;

export type PluginAvatar = RenderableExtensionPointDefinition<
  "plugins.plugin-avatar",
  {
    plugin: Plugin;
  }
>;

export type PrimaryNavigation = RenderableExtensionPointDefinition<"primary-navigation", { links: Links }>;

/**
 * - A placeholder for the first navigation menu.
 * - A PrimaryNavigationLink Component can be used here
 * - Actually this Extension Point is used from the Activity Plugin to display the activities at the first Main Navigation menu.
 */
export type PrimaryNavigationFirstMenu = RenderableExtensionPointDefinition<
  "primary-navigation.first-menu",
  { links: Links; label: string }
>;

export type ProfileRoute = RenderableExtensionPointDefinition<"profile.route", { me: Me; url: string }>;
export type ProfileSetting = RenderableExtensionPointDefinition<
  "profile.setting",
  { me?: Me; url: string; links: Links }
>;

export type RepoConfigRoute = RenderableExtensionPointDefinition<
  "repo-config.route",
  { repository: Repository; url: string }
>;

export type RepoConfigDetails = RenderableExtensionPointDefinition<
  "repo-config.details",
  { repository: Repository; url: string }
>;

export type ReposBranchDetailsInformation = RenderableExtensionPointDefinition<
  "repos.branch-details.information",
  { repository: Repository; branch: Branch }
>;

/**
 * - Location: At meta data view for file
 * - can be used to render additional meta data line
 * - Props: file: string, repository: Repository, revision: string
 */
export type ReposContentMetaData = RenderableExtensionPointDefinition<
  "repos.content.metadata",
  { file: File; repository: Repository; revision: string }
>;

export type ReposCreateNamespace = RenderableExtensionPointDefinition<
  "repos.create.namespace",
  {
    label: string;
    helpText: string;
    value: string;
    onChange: (namespace: string) => void;
    errorMessage: string;
    validationError: boolean;
  }
>;

export type ReposSourcesContentActionBar = RenderableExtensionPointDefinition<
  "repos.sources.content.actionbar",
  {
    repository: Repository;
    file: File;
    revision: string;
    handleExtensionError: React.Dispatch<React.SetStateAction<Error | undefined>>;
  }
>;

export type RepositoryNavigation = RenderableExtensionPointDefinition<
  "repository.navigation",
  { repository: Repository; url: string; indexLinks: Links }
>;

export type RepositoryNavigationTopLevel = RenderableExtensionPointDefinition<
  "repository.navigation.topLevel",
  { repository: Repository; url: string; indexLinks: Links }
>;

export type RepositoryRoleDetailsInformation = RenderableExtensionPointDefinition<
  "repositoryRole.role-details.information",
  { role: RepositoryRole }
>;

export type RepositorySetting = RenderableExtensionPointDefinition<
  "repository.setting",
  { repository: Repository; url: string; indexLinks: Links }
>;

export type RepositoryAvatar = RenderableExtensionPointDefinition<
  "repos.repository-avatar",
  { repository: Repository }
>;

/**
 * - Location: At each repository in repository overview
 * - can be used to add avatar for each repository (e.g., to mark repository type)
 */
export type PrimaryRepositoryAvatar = RenderableExtensionPointDefinition<
  "repos.repository-avatar.primary",
  { repository: Repository }
>;

/**
 * - Location: At bottom of a single repository view
 * - can be used to show detailed information about the repository (how to clone, e.g.)
 */
export type RepositoryDetailsInformation = RenderableExtensionPointDefinition<
  "repos.repository-details.information",
  { repository: Repository }
>;

export type RepositorySourcesView = RenderableExtensionPointDefinition<
  "repos.sources.view",
  { file: File; contentType: string; revision: string; basePath: string }
>;

export type RolesRoute = RenderableExtensionPointDefinition<
  "roles.route",
  { role: HalRepresentation & RepositoryRoleBase & { creationDate?: string; lastModified?: string }; url: string }
>;

export type UserRoute = RenderableExtensionPointDefinition<"user.route", { user: User; url: string }>;
export type UserSetting = RenderableExtensionPointDefinition<"user.setting", { user: User; url: string }>;

/**
 * - Dynamic extension point for custom language-specific renderers
 * - Overrides the default Syntax Highlighter for the given language
 * - Used by the Markdown Plantuml Plugin
 */
export type MarkdownCodeRenderer<Language extends string | undefined = undefined> =
  SimpleRenderableDynamicExtensionPointDefinition<
    "markdown-renderer.code.",
    Language,
    {
      language: Language extends string ? Language : string;
      value: string;
      indexLinks: Links;
    }
  >;

/**
 * - Define custom protocols and their renderers for links in markdown
 *
 * Example:
 * ```markdown
 * [description](myprotocol:somelink)
 * ```
 *
 * ```typescript
 * binder.bind<extensionPoints.MarkdownLinkProtocolRenderer<"myprotocol">>("markdown-renderer.link.protocol", { protocol: "myprotocol", renderer: MyProtocolRenderer })
 * ```
 */
export type MarkdownLinkProtocolRenderer<Protocol extends string | undefined = undefined> = ExtensionPointDefinition<
  "markdown-renderer.link.protocol",
  {
    protocol: Protocol extends string ? Protocol : string;
    renderer: React.ComponentType<{
      protocol: Protocol extends string ? Protocol : string;
      href: string;
    }>;
  }
>;
