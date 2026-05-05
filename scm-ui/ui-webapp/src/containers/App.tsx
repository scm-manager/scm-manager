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

import React, { FC } from "react";
import { createBrowserRouter, redirect, type RouteObject } from "react-router-dom";
import { indexLoader } from "@scm-manager/ui-api";
import { ErrorBoundary, ProtectedRoute, urls } from "@scm-manager/ui-components";
import {
  ADMIN_BASE_ROUTE,
  ADMIN_PLUGINS_SUB_ROUTES,
  ADMIN_ROLE_COLLECTION_SUB_ROUTES,
  ADMIN_SETTINGS_SUB_ROUTES,
  ADMIN_SUB_ROUTES,
  GROUP_COLLECTION_BASE_ROUTE,
  GROUP_COLLECTION_SUB_ROUTES,
  HELP_BASE_ROUTE,
  HELP_SUB_ROUTES,
  IMPORT_LOG_BASE_ROUTE,
  ME_BASE_ROUTE,
  ME_SETTINGS_SUB_ROUTES,
  ME_SUB_ROUTES,
  REPOSITORY_BRANCHES_SUB_ROUTES,
  REPOSITORY_CODE_SUB_ROUTES,
  REPOSITORY_COLLECTION_BASE_ROUTE,
  REPOSITORY_COLLECTION_SUB_ROUTES,
  REPOSITORY_SETTINGS_SUB_ROUTES,
  REPOSITORY_SUB_ROUTES,
  SINGLE_GROUP_BASE_ROUTE,
  SINGLE_NAMESPACE_BASE_ROUTE,
  SINGLE_REPOSITORY_BASE_ROUTE,
  SINGLE_USER_BASE_ROUTE,
  USER_COLLECTION_BASE_ROUTE,
  USER_COLLECTION_SUB_ROUTES,
} from "@scm-manager/ui-core";
import { extensionPoints, useRouteExtensions } from "@scm-manager/ui-extensions";
import { RouterProvider } from "react-router";
import { RootLayout } from "./RootLayout";
import Sources from "../repos/sources/containers/Sources";
import ChangesetView from "../repos/containers/ChangesetView";
import ChangesetsRoot from "../repos/containers/ChangesetsRoot";
import Changesets from "../repos/containers/Changesets";
import CompareRoot from "../repos/compare/CompareRoot";
import FileSearch from "../repos/codeSection/containers/FileSearch";
import SourceExtensions from "../repos/sources/containers/SourceExtensions";
import BranchRoot from "../repos/branches/containers/BranchRoot";
import RepositoryDetails from "../repos/components/RepositoryDetails";
import BranchesOverview from "../repos/branches/containers/BranchesOverview";
import CreateBranch from "../repos/branches/containers/CreateBranch";
import TagRoot from "../repos/tags/container/TagRoot";
import TagsOverview from "../repos/tags/container/TagsOverview";
import EditRepo from "../repos/containers/EditRepo";
import { NamespacePermissions, RepositoryPermissions } from "../repos/permissions/containers/Permissions";
import NamespaceInformation from "../repos/namespaces/containers/NamespaceInformation";
import AdminDetails from "../admin/containers/AdminDetails";
import PluginsOverview from "../admin/plugins/containers/PluginsOverview";
import CreateRepositoryRole from "../admin/roles/containers/CreateRepositoryRole";
import RepositoryRoles from "../admin/roles/containers/RepositoryRoles";
import SingleRepositoryRole from "../admin/roles/containers/SingleRepositoryRole";
import GlobalConfig from "../admin/containers/GlobalConfig";
import ProfileInfo from "./ProfileInfo";
import Theme from "./Theme";
import Accessibility from "./Accessibility";
import ChangeUserPassword from "./ChangeUserPassword";
import { SetMyPublicKeys } from "../users/components/publicKeys/SetPublicKeys";
import { SetMyApiKeys } from "../users/components/apiKeys/SetApiKeys";
import CompareView from "../repos/compare/CompareView";
import { IndexResources } from "@scm-manager/ui-types";

const App: FC = () => {
  // auth routes
  const Login = React.lazy(() => import("../containers/Login"));
  const Logout = React.lazy(() => import("../containers/Logout"));

  // repo routes
  const Overview = React.lazy(() => import("../repos/containers/Overview"));
  const RepositoryRoot = React.lazy(() => import("../repos/containers/RepositoryRoot"));
  const NamespaceRoot = React.lazy(() => import("../repos/namespaces/containers/NamespaceRoot"));
  const CreateRepositoryRoot = React.lazy(() => import("../repos/containers/CreateRepositoryRoot"));

  // user routes
  const Users = React.lazy(() => import("../users/containers/Users"));
  const CreateUser = React.lazy(() => import("../users/containers/CreateUser"));
  const SingleUser = React.lazy(() => import("../users/containers/SingleUser"));

  const Groups = React.lazy(() => import("../groups/containers/Groups"));
  const SingleGroup = React.lazy(() => import("../groups/containers/SingleGroup"));
  const CreateGroup = React.lazy(() => import("../groups/containers/CreateGroup"));

  const Admin = React.lazy(() => import("../admin/containers/Admin"));

  const Profile = React.lazy(() => import("./Profile"));

  const ImportLog = React.lazy(() => import("../repos/importlog/ImportLog"));
  const Search = React.lazy(() => import("../search/Search"));
  const Syntax = React.lazy(() => import("../search/Syntax"));
  const ExternalError = React.lazy(() => import("./ExternalError"));

  const repos = REPOSITORY_COLLECTION_BASE_ROUTE;
  const create = REPOSITORY_COLLECTION_SUB_ROUTES.CREATE;
  const import_ = REPOSITORY_COLLECTION_SUB_ROUTES.IMPORT;
  const repo = SINGLE_REPOSITORY_BASE_ROUTE;
  const info = REPOSITORY_SUB_ROUTES.INFO;
  const code = REPOSITORY_SUB_ROUTES.CODE;
  const sources = REPOSITORY_CODE_SUB_ROUTES.SOURCES;
  const changesets = REPOSITORY_CODE_SUB_ROUTES.CHANGESETS;
  const changeset = REPOSITORY_CODE_SUB_ROUTES.CHANGESET;
  const branches = REPOSITORY_SUB_ROUTES.BRANCHES;
  const branch = REPOSITORY_SUB_ROUTES.BRANCH;
  const tags = REPOSITORY_SUB_ROUTES.TAGS;
  const tag = REPOSITORY_SUB_ROUTES.TAG;
  const compare = REPOSITORY_SUB_ROUTES.COMPARE;
  const search = REPOSITORY_SUB_ROUTES.SEARCH;
  const settings = REPOSITORY_SUB_ROUTES.SETTINGS;
  const sourceext = REPOSITORY_SUB_ROUTES.SOURCEEXT;

  const branchCreate = REPOSITORY_BRANCHES_SUB_ROUTES.CREATE;

  const general = REPOSITORY_SETTINGS_SUB_ROUTES.GENERAL;
  const permissions = REPOSITORY_SETTINGS_SUB_ROUTES.PERMISSIONS;

  const namespace = SINGLE_NAMESPACE_BASE_ROUTE;
  const namespaceInfo = REPOSITORY_SUB_ROUTES.INFO;
  const namespaceSettings = REPOSITORY_SUB_ROUTES.SETTINGS;
  const namespacePermissions = REPOSITORY_SETTINGS_SUB_ROUTES.PERMISSIONS;

  const users = USER_COLLECTION_BASE_ROUTE;
  const user = SINGLE_USER_BASE_ROUTE;
  const userCreate = USER_COLLECTION_SUB_ROUTES.CREATE;

  const groups = GROUP_COLLECTION_BASE_ROUTE;
  const group = SINGLE_GROUP_BASE_ROUTE;
  const groupCreate = GROUP_COLLECTION_SUB_ROUTES.CREATE;

  const admin = ADMIN_BASE_ROUTE;
  const adminGeneral = ADMIN_SETTINGS_SUB_ROUTES.GENERAL;
  const adminInfo = ADMIN_SUB_ROUTES.INFO;
  const plugins = ADMIN_SUB_ROUTES.PLUGINS;
  const available = ADMIN_PLUGINS_SUB_ROUTES.AVAILABLE;
  const installed = ADMIN_PLUGINS_SUB_ROUTES.INSTALLED;
  const roles = ADMIN_SUB_ROUTES.ROLES;
  const role = ADMIN_SUB_ROUTES.ROLE;
  const rolesCreate = ADMIN_ROLE_COLLECTION_SUB_ROUTES.CREATE;

  const me_ = ME_BASE_ROUTE;
  const meInfo = ME_SUB_ROUTES.INFO;
  const meSettings = ME_SUB_ROUTES.SETTINGS;
  const meTheme = ME_SETTINGS_SUB_ROUTES.THEME;
  const meAccessibility = ME_SETTINGS_SUB_ROUTES.ACCESSIBILITY;
  const mePassword = ME_SETTINGS_SUB_ROUTES.PASSWORD;
  const mePublicKeys = ME_SETTINGS_SUB_ROUTES.PUBLIC_KEYS;
  const meApiKeys = ME_SETTINGS_SUB_ROUTES.API_KEYS;

  const importlog = IMPORT_LOG_BASE_ROUTE;

  const help = HELP_BASE_ROUTE;
  const searchSyntax = HELP_SUB_ROUTES.SEARCH_SYNTAX;

  const authenticated = true;

  const mainExtensionRoutes = useRouteExtensions<extensionPoints.MainRoute>({
    name: "main.route",
  });
  const redirectExtensionRoutes = useRouteExtensions({
    name: "main.redirect",
  });
  const repositoryRoutes = useRouteExtensions<extensionPoints.RepositoryRoute>({
    name: "repository.route",
  });
  const adminSettingsExtensionRoutes = useRouteExtensions({
    name: "admin.settings.route",
  });
  const adminExtensionRoutes = useRouteExtensions<extensionPoints.AdminRoute>({
    name: "admin.route",
  });
  const profileExtensionRoutes = useRouteExtensions<extensionPoints.ProfileRoute>({
    name: "profile.route",
  });
  const namespaceExtensionRoutes = useRouteExtensions({
    name: "namespace.route",
  });
  const editRepositoryExtensionRoutes = useRouteExtensions<extensionPoints.RepoConfigRoute>({
    name: "repo-config.route",
  });

  const indexRoute = () => {
    const extensions = redirectExtensionRoutes as string[];
    if (extensions.length > 0) {
      return redirect(extensions[0]);
    }
    return redirect("/repos/");
  };

  const redirectWithoutLoginLoader = async () => {
    const data = (await indexLoader()) as IndexResources;
    if (data._links.login && !data._links.me) {
      // not logged in and no anonymous mode
      return redirect("/login");
    }
    return data;
  };

  const routes: RouteObject[] = [
    {
      element: <RootLayout />,
      errorElement: <ErrorBoundary />,
      children: [
        {
          path: "/login",
          element: <Login />,
          loader: indexLoader,
        },
        {
          path: "/logout",
          element: <Logout />,
          loader: indexLoader,
        },
        {
          path: "/error/:code",
          element: <ExternalError />,
        },
        {
          path: "/",
          id: "index",
          errorElement: <ErrorBoundary />,
          loader: redirectWithoutLoginLoader,
          children: [
            {
              index: true,
              loader: indexRoute,
            },
            {
              path: `${repos}/${import_}`,
              loader: () => redirect(`/${repos}/${create}/${import_}`),
            },
            {
              path: "users",
              loader: () => redirect("/users/"),
            },
            {
              path: "groups",
              loader: () => redirect("/groups/"),
            },
            {
              path: "search/:type",
              loader: async ({ request }) => {
                const url = new URL(request.url);
                const targetUrl = new URL(".", url);
                return redirect(targetUrl.pathname + targetUrl.search);
              },
            },
            {
              element: <ProtectedRoute authenticated={authenticated} />,
              children: [
                {
                  path: `${repos}/`,
                  element: <Overview />,
                },
                {
                  path: `${repos}/${create}/*`,
                  element: <CreateRepositoryRoot />,
                },
                {
                  path: `${repos}/:namespace`,
                  element: <Overview />,
                },
                {
                  path: `${repos}/:namespace/:page`,
                  element: <Overview />,
                },
                {
                  path: `${repo}/`,
                  loader: () => redirect(`/${repos}/`),
                },
                {
                  path: `${repo}/:namespace/:name/*`,
                  element: <RepositoryRoot />,
                  children: [
                    {
                      index: true,
                      loader: () => redirect(`${code}/${sources}`),
                    },
                    {
                      path: code,
                      children: [
                        {
                          index: true,
                          loader: () => redirect(sources),
                        },
                        {
                          path: `${sources}/:sourceName?/*`,
                          element: <Sources />,
                        },
                        {
                          path: `${changeset}/:id`,
                          element: <ChangesetView />,
                        },
                        {
                          path: changesets,
                          element: <ChangesetsRoot />,
                          children: [
                            {
                              index: true,
                              element: <Changesets />,
                            },
                            {
                              path: ":page",
                              element: <Changesets />,
                            },
                          ],
                        },
                        {
                          path: `:sourceType/:sourceName/${changesets}/`,
                          element: <ChangesetsRoot />,
                          children: [
                            {
                              index: true,
                              element: <Changesets />,
                            },
                            {
                              path: ":page",
                              element: <Changesets />,
                            },
                          ],
                        },
                        {
                          path: `${compare}/:sourceType/:sourceName/*`,
                          children: [
                            {
                              index: true,
                              element: <CompareRoot />,
                            },
                            {
                              path: ":targetType/:targetName/*",
                              element: <CompareView />,
                            },
                          ],
                          element: <CompareRoot />,
                        },
                        {
                          path: `${search}/:sourceName/`,
                          element: <FileSearch />,
                        },
                        {
                          path: `${sourceext}/:extension/`,
                          children: [
                            {
                              index: true,
                              element: <SourceExtensions />,
                            },
                            {
                              path: ":revision/*",
                              element: <SourceExtensions />,
                            },
                          ],
                        },
                      ],
                    },
                    {
                      path: sources,
                      children: [{ path: ":revision?/:path/*", element: null }],
                    },
                    {
                      path: `${changeset}/:id`,
                      loader: () => redirect(`${code}/${changeset}/:id`),
                    },
                    {
                      path: changesets,
                      loader: () => redirect(`${code}/${changesets}`),
                    },
                    {
                      path: `${branch}/:branch/*`,
                      element: <BranchRoot />,
                    },
                    {
                      path: info,
                      element: <RepositoryDetails />,
                    },
                    {
                      path: branches,
                      element: <BranchesOverview />,
                    },
                    {
                      path: `${branches}/${branchCreate}`,
                      element: <CreateBranch />,
                    },
                    {
                      path: `${tag}/:tag/*`,
                      children: [
                        {
                          index: true,
                          loader: () => redirect("info"),
                        },
                        {
                          path: "info",
                          element: <TagRoot />,
                        },
                      ],
                    },
                    {
                      path: tags,
                      element: <TagsOverview />,
                    },
                    {
                      path: settings,
                      children: [
                        {
                          index: true,
                          loader: () => redirect(general),
                        },
                        {
                          path: general,
                          element: <EditRepo />,
                        },
                        ...editRepositoryExtensionRoutes,
                        {
                          path: permissions,
                          element: <RepositoryPermissions />,
                        },
                      ],
                    },
                    ...repositoryRoutes,
                  ],
                },
                {
                  path: `${namespace}/:namespace/*`,
                  element: <NamespaceRoot />,
                  children: [
                    {
                      path: namespaceInfo,
                      element: <NamespaceInformation />,
                    },
                    {
                      path: `${namespaceSettings}/${namespacePermissions}`,
                      element: <NamespacePermissions />,
                    },
                    ...namespaceExtensionRoutes,
                  ],
                },
                {
                  path: `${users}/`,
                  element: <Users />,
                },
                {
                  path: `${users}/${userCreate}`,
                  element: <CreateUser />,
                },
                {
                  path: `${users}/:page`,
                  element: <Users />,
                },
                {
                  path: `${user}/`,
                  loader: () => redirect(`/${users}/`),
                },
                {
                  path: `${user}/:name/*`,
                  element: <SingleUser />,
                },
                {
                  path: `${groups}/`,
                  element: <Groups />,
                },
                {
                  path: `${group}/`,
                  loader: () => redirect(`/${groups}/`),
                },
                {
                  path: `${group}/:name/*`,
                  element: <SingleGroup />,
                },
                {
                  path: `${groups}/${groupCreate}`,
                  element: <CreateGroup />,
                },
                {
                  path: `${groups}/:page`,
                  element: <Groups />,
                },
                {
                  path: `${admin}/*`,
                  element: <Admin />,
                  children: [
                    {
                      index: true,
                      loader: () => redirect("info"),
                    },
                    {
                      path: adminInfo,
                      element: <AdminDetails />,
                    },
                    {
                      path: plugins,
                      children: [
                        {
                          index: true,
                          loader: () => redirect("installed/"),
                        },
                        {
                          path: `${installed}/:page?`,
                          element: <PluginsOverview installed={true} />,
                        },
                        {
                          path: `${available}/:page?`,
                          element: <PluginsOverview installed={false} />,
                        },
                      ],
                    },
                    {
                      path: roles,
                      children: [
                        {
                          path: rolesCreate,
                          element: <CreateRepositoryRole />,
                        },
                        {
                          path: ":page?",
                          element: <RepositoryRoles baseUrl={`${admin}/${roles}`} />,
                        },
                        {
                          path: `:page?/${rolesCreate}`,
                          loader: () => redirect(`../${rolesCreate}`),
                        },
                      ],
                    },
                    {
                      path: `${role}/:role/*`,
                      element: <SingleRepositoryRole />,
                    },
                    {
                      path: settings,
                      children: [
                        {
                          index: true,
                          loader: () => redirect(adminGeneral),
                        },
                        {
                          path: adminGeneral,
                          element: <GlobalConfig />,
                        },
                        ...adminSettingsExtensionRoutes,
                      ],
                    },
                    ...adminExtensionRoutes,
                  ],
                },
                {
                  path: `${me_}/*`,
                  element: <Profile />,
                  children: [
                    {
                      index: true,
                      loader: () => redirect(meInfo),
                    },
                    {
                      path: meInfo,
                      element: <ProfileInfo />,
                    },
                    {
                      path: meSettings,
                      children: [
                        {
                          index: true,
                          loader: () => redirect(meTheme),
                        },
                        {
                          path: meTheme,
                          element: <Theme />,
                        },
                        {
                          path: meAccessibility,
                          element: <Accessibility />,
                        },
                        {
                          path: mePassword,
                          element: <ChangeUserPassword />,
                        },
                        {
                          path: mePublicKeys,
                          element: <SetMyPublicKeys />,
                        },
                        {
                          path: meApiKeys,
                          element: <SetMyApiKeys />,
                        },
                        ...profileExtensionRoutes,
                      ],
                    },
                  ],
                },
                {
                  path: `${importlog}/:logId`,
                  element: <ImportLog />,
                },
                {
                  path: `${search}/:type/:page`,
                  element: <Search />,
                },
                {
                  path: `${search}/:type/`,
                  element: <Search />,
                },
                {
                  path: `${help}/${searchSyntax}`,
                  element: <Syntax />,
                },
                ...mainExtensionRoutes,
              ],
            },
          ],
        },
      ],
    },
  ];

  return <RouterProvider router={createBrowserRouter(routes, { basename: urls.contextPath })} />;
};

export default App;
