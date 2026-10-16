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

import React, { ComponentProps, ComponentType, ReactNode } from "react";
import {
  Branch,
  BranchDetails,
  Changeset,
  ContentType,
  File,
  Group,
  HalRepresentation,
  Hit,
  IndexResources,
  Links,
  Me,
  Namespace,
  NamespaceStrategies,
  Person,
  Plugin,
  Repository,
  RepositoryCreation,
  RepositoryRole,
  RepositoryRoleBase,
  RepositoryTypeCollection,
  Tag,
  User,
} from "@scm-manager/ui-types";
import { ExtensionPointDefinition } from "./binder";
import { RenderableExtensionPointDefinition, SimpleRenderableDynamicExtensionPointDefinition } from "./ExtensionPoint";
import { RouteExtensionPointDefinition } from "./useRouteExtensions";

type RepositoryCreatorSubFormProps = {
  repository: RepositoryCreation;
  onChange: (repository: RepositoryCreation) => void;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

export type RepositoryCreatorComponentProps = ComponentProps<RepositoryCreator["type"]["component"]>;

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

export type RepositoryFlags = RenderableExtensionPointDefinition<
  "repository.flags",
  { repository: Repository; tooltipLocation?: "bottom" | "right" | "top" | "left" }
>;

export type ReposSourcesActionbar = RenderableExtensionPointDefinition<
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

export type ReposSourcesEmptyActionbar = RenderableExtensionPointDefinition<
  "repos.sources.empty.actionbar",
  {
    sources: File;
    repository: Repository;
  }
>;

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

export type ReposSourcesTreeRowRight = RenderableExtensionPointDefinition<
  "repos.sources.tree.row.right",
  ReposSourcesTreeRowProps
>;

export type ReposSourcesTreeRowAfter = RenderableExtensionPointDefinition<
  "repos.sources.tree.row.after",
  ReposSourcesTreeRowProps
>;

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

export type PrimaryNavigationLogoutButton = RenderableExtensionPointDefinition<
  "primary-navigation.logout",
  {
    links: Links;
    label: string;
    className: string;
    content: React.ReactNode;
  }
>;

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

export type RepositoryOverviewTop = RenderableExtensionPointDefinition<
  "repository.overview.top",
  {
    page: number;
    search?: string;
    namespace?: string;
  }
>;

export type RepositoryOverviewLeft = ExtensionPointDefinition<"repository.overview.left", React.ComponentType>;

export type RepositoryOverviewTitle = RenderableExtensionPointDefinition<"repository.overview.title">;

export type RepositoryOverviewSubtitle = RenderableExtensionPointDefinition<"repository.overview.subtitle">;

/**
 * Specify options for the repository overview like the page size
 */
export type RepositoryOverviewListOptionsExtensionPoint = ExtensionPointDefinition<
  "repository.overview.listOptions",
  () => { pageSize?: number; showArchived?: boolean }
>;

export type DocumentTitleExtensionPoint = ExtensionPointDefinition<
  "document.title",
  { documentTitle: string | ((originalTitle: string) => string) }
>;

// From docs

export type AdminNavigation = RenderableExtensionPointDefinition<"admin.navigation", { links: Links }>;

export type AdminRoute = RouteExtensionPointDefinition<"admin.route">;

export type AdminSetting = RenderableExtensionPointDefinition<"admin.setting", { links?: Links }>;

/**
 * - Can be used to replace parts of a changeset description with components
 * - Has to be bound with a funktion taking the changeset and the (partial) description and returning `Replacement` objects with the following attributes:
 * - textToReplace: The text part of the description that should be replaced by a component
 * - replacement: The component to take instead of the text to replace
 * - replaceAll: Optional boolean; if set to `true`, all occurances of the text will be replaced (default: `false`)
 */
export type ChangesetDescriptionTokens = ExtensionPointDefinition<
  "changeset.description.tokens",
  (
    changeset: Changeset,
    value: string,
  ) => Array<{
    textToReplace: string;
    replacement: ReactNode;
    replaceAll?: boolean;
  }>,
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

export type ChangesetsContributorTableRow = RenderableExtensionPointDefinition<
  "changesets.contributor.table.row",
  { changeset: Changeset }
>;

export type GroupNavigation = RenderableExtensionPointDefinition<"group.navigation", { group: Group }>;

export type GroupRoute = RouteExtensionPointDefinition<"group.route", { group?: Group }>;
export type GroupSetting = RenderableExtensionPointDefinition<"group.setting", { group?: Group }>;

/**
 * - Add a new Route to the main Route (scm/)
 * - Props: authenticated?: boolean, links: Links
 */
export type MainRoute = RouteExtensionPointDefinition<"main.route">;

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

export type ProfileRoute = RouteExtensionPointDefinition<"profile.route">;
export type ProfileSetting = RenderableExtensionPointDefinition<"profile.setting", { me?: Me; links: Links }>;

export type RepoConfigRoute = RouteExtensionPointDefinition<"repo-config.route">;

export type RepoConfigDetails = RenderableExtensionPointDefinition<"repo-config.details", { repository?: Repository }>;

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
    validationError?: boolean;
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

export type RepositoryCodeOverviewContent = RenderableExtensionPointDefinition<
  "repository.code.overview.content",
  { sources: File; repository: Repository }
>;

export type RepositoryCodeOverviewBanner = RenderableExtensionPointDefinition<
  "repository.code.overview.banner",
  { sources: File; repository: Repository }
>;

export type RepositoryCodeOverviewNoSourcesBanner = RenderableExtensionPointDefinition<
  "repository.code.overview.noSources.banner",
  { repository: Repository }
>;

export type RepositoryNavigation = RenderableExtensionPointDefinition<
  "repository.navigation",
  { repository: Repository; indexLinks: Links }
>;

export type RepositoryNavigationTopLevel = RenderableExtensionPointDefinition<
  "repository.navigation.topLevel",
  { repository: Repository; indexLinks: Links }
>;

export type RepositoryRoleDetailsInformation = RenderableExtensionPointDefinition<
  "repositoryRole.role-details.information",
  { role: RepositoryRole }
>;

export type RepositorySetting = RenderableExtensionPointDefinition<
  "repository.setting",
  { repository: Repository; indexLinks: Links }
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
  { repository: Repository; size: number }
>;

/**
 * - Location: At bottom of a single repository view
 * - can be used to show detailed information about the repository (how to clone, e.g.)
 */
export type RepositoryDetailsInformation = RenderableExtensionPointDefinition<
  "repos.repository-details.information",
  { repository: Repository }
>;

/**
 * - Location: At sources viewer
 * - can  be used to render a special source that is not an image or a source code
 */
export type RepositorySourcesView = RenderableExtensionPointDefinition<
  "repos.sources.view",
  { file: File; contentType: string; revision: string; basePath: string }
>;

export type RolesRoute = RouteExtensionPointDefinition<
  "roles.route",
  { role?: HalRepresentation & RepositoryRoleBase & { creationDate?: string; lastModified?: string } }
>;

export type UserRoute = RouteExtensionPointDefinition<"user.route", { user?: User }>;
export type UserSetting = RenderableExtensionPointDefinition<"user.setting", { user?: User }>;

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
      language?: Language extends string ? Language : string;
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

/**
 * Used to determine an avatar image url from a given {@link Person}.
 *
 * @see https://github.com/scm-manager/scm-gravatar-plugin
 */
export type AvatarFactory = ExtensionPointDefinition<"avatar.factory", (person: Person) => string | undefined>;

type MainRedirectProps = {
  me: Me;
  authenticated?: boolean;
};

/**
 * - Extension Point for a link factory that provide the Redirect Link
 * - Actually used from the activity plugin: binder.bind("main.redirect", () => "/activity");
 */
export type MainRedirect = ExtensionPointDefinition<
  "main.redirect",
  (props: MainRedirectProps) => string,
  MainRedirectProps
>;

export type RepositoryCardBeforeTitle = RenderableExtensionPointDefinition<
  "repository.card.beforeTitle",
  { repository: Repository }
>;

export type RepositoryCreationInitialization = RenderableExtensionPointDefinition<
  "repos.create.initialize",
  {
    repository: Repository;
    setCreationContextEntry: (key: string, value: any) => void;
    indexResources: Partial<HalRepresentation> & {
      links?: Links;
      version?: string;
      initialization?: string;
    };
  }
>;

export type NamespaceTopLevelNavigation = RenderableExtensionPointDefinition<
  "namespace.navigation.topLevel",
  { namespace: Namespace }
>;

export type NamespaceRoute = RenderableExtensionPointDefinition<"namespace.route">;

export type NamespaceSetting = RenderableExtensionPointDefinition<
  "namespace.setting",
  { namespace: Namespace; indexLinks: Links }
>;

export type RepositoryTagDetailsInformation = RenderableExtensionPointDefinition<
  "repos.tag-details.information",
  { repository: Repository; tag: Tag }
>;

export type SearchHitRenderer<Type extends string | undefined = undefined> = RenderableExtensionPointDefinition<
  Type extends string ? `search.hit.${Type}.renderer` : `search.hit.${string}.renderer`,
  { hit: Hit }
>;

export type RepositorySourcesContentDownloadButton = RenderableExtensionPointDefinition<
  "repos.sources.content.downloadButton",
  { repository: Repository; file: File }
>;

export type RepositoryRoute = RouteExtensionPointDefinition<"repository.route">;

type RepositoryRedirectProps = {
  namespace: string;
  name: string;
  repository: Repository;
  loading: false;
  error: null;
  repoLink: string;
  indexLinks: Links;
  match: {
    params: {
      namespace: string;
      name: string;
    };
    isExact: boolean;
    path: string;
  };
};

export type RepositoryRedirect = ExtensionPointDefinition<
  "repository.redirect",
  (props: RepositoryRedirectProps) => string,
  RepositoryRedirectProps
>;

export type InitializationStep<Step extends string | undefined = undefined> =
  SimpleRenderableDynamicExtensionPointDefinition<
    "initialization.step.",
    Step,
    {
      data: HalRepresentation;
    }
  >;

export type ContentActionExtensionProps = {
  repository: Repository;
  file: File;
  revision: string;
  handleExtensionError: React.Dispatch<React.SetStateAction<Error | undefined>>;
  contentType?: ContentType;
  setLoading?: (isLoading: boolean) => void;
};

type BaseActionBarOverflowMenuProps = {
  category: string;
  label: string;
  icon: string;
  props?: Record<string | number | symbol, unknown>;
};

export type ActionMenuProps = BaseActionBarOverflowMenuProps & { action: (props: ContentActionExtensionProps) => void };
export type ModalMenuProps = BaseActionBarOverflowMenuProps & {
  modalElement: ComponentType<ContentActionExtensionProps & { close: () => void }>;
};
export type LinkMenuProps = BaseActionBarOverflowMenuProps & { link: (props: ContentActionExtensionProps) => string };

export type FileViewActionBarOverflowMenu = ExtensionPointDefinition<
  "repos.sources.content.actionbar.menu",
  ActionMenuProps | ModalMenuProps | LinkMenuProps,
  ContentActionExtensionProps
>;

export type LoginForm = RenderableExtensionPointDefinition<"login.form">;

export type RepositoryDeleteButton = RenderableExtensionPointDefinition<
  "repository.deleteButton",
  { repository: Repository }
>;

export type RepositoryDangerZone = RenderableExtensionPointDefinition<
  "repository.dangerZone",
  { repository: Repository }
>;

export type RepositoryInformationTableBottom = RenderableExtensionPointDefinition<
  "repository.information.table.bottom",
  { repository: Repository }
>;

export type RepositoryBanner = RenderableExtensionPointDefinition<"repository.banner", { repository: Repository }>;

export type UserInformationTableBottom = RenderableExtensionPointDefinition<
  "user.information.table.bottom",
  { user: User }
>;

export type GroupInformationTableBottom = RenderableExtensionPointDefinition<
  "group.information.table.bottom",
  { group: Group }
>;

type BranchMenuDetailProps = {
  repository: Repository;
  branch: Branch;
};

/**
 * @since 2.46.0
 * @example
 * ```typescript
 * import { Icon, Menu } from "@scm-manager/ui-core";
 *
 * binder.bind<extensionPoints.BranchListMenu>(
 *  "branches.list.menu",
 *  <Menu.Link to="/code/sources/etc">
 *   <Icon>code</Icon>
 *   <span>Jump to source</span>
 *  </Menu.Link>
 * )
 * ```
 */
export type BranchListMenu = RenderableExtensionPointDefinition<"branches.list.menu", BranchMenuDetailProps>;

type BranchListDetailProps = {
  repository: Repository;
  branch: Branch;
  branchDetails?: BranchDetails;
};

/**
 * @since 2.46.0
 * @example
 * ```typescript
 * import { Card } from "@scm-manager/ui-core";
 * import { extensionPoints, binder } from "@scm-manager/ui-extensions";
 *
 * binder.bind<extensionPoints.BranchListDetail>(
 *  "branches.list.detail",
 *  <Card.Details.Detail>
 *   <Card.Details.Detail.Label>Reviewers</Card.Details.Detail.Label>
 *   <Card.Details.Detail.Tag>2 / 3</Card.Details.Detail.Tag>
 *  </Card.Details.Detail>
 * )
 * ```
 */
export type BranchListDetail = RenderableExtensionPointDefinition<"branches.list.detail", BranchListDetailProps>;
