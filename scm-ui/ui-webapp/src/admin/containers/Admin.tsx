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
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, RouteProps, Switch, useRouteMatch } from "react-router-dom";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import {
  CustomQueryFlexWrappedColumns,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  StateMenuContextProvider,
  SubNavigation,
  urls,
} from "@scm-manager/ui-components";
import AdminDetails from "./AdminDetails";
import PluginsOverview from "../plugins/containers/PluginsOverview";
import GlobalConfig from "./GlobalConfig";
import RepositoryRoles from "../roles/containers/RepositoryRoles";
import SingleRepositoryRole from "../roles/containers/SingleRepositoryRole";
import CreateRepositoryRole from "../roles/containers/CreateRepositoryRole";
import { useIndexLinks } from "@scm-manager/ui-api";

const Admin: FC = () => {
  const links = useIndexLinks();
  const match = useRouteMatch();
  const [t] = useTranslation("admin");
  const availablePluginsLink = links.availablePlugins;
  const installedPluginsLink = links.installedPlugins;

  const matchesRoles = (route: RouteProps) => {
    const url = urls.matchedUrlFromMatch(match);
    const regex = new RegExp(`${url}/role/`);
    return !!route.location?.pathname.match(regex);
  };

  const url = urls.matchedUrlFromMatch(match);
  const extensionProps = {
    links,
    url,
  };

  return (
    <StateMenuContextProvider>
      <Page>
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Switch>
              <Redirect exact from={url} to={`${url}/info`} />
              <Route path={`${url}/info`} exact component={AdminDetails} />
              <Route path={`${url}/settings/general`} exact component={GlobalConfig} />
              <Redirect exact from={`${url}/plugins`} to={`${url}/plugins/installed/`} />
              <Route path={`${url}/plugins/installed`} exact>
                <PluginsOverview installed={true} />
              </Route>
              <Route path={`${url}/plugins/installed/:page`} exact>
                <PluginsOverview installed={true} />
              </Route>
              <Route path={`${url}/plugins/available`} exact>
                <PluginsOverview installed={false} />
              </Route>
              <Route path={`${url}/plugins/available/:page`} exact>
                <PluginsOverview installed={false} />
              </Route>
              <Route path={`${url}/role/:role`}>
                <SingleRepositoryRole />
              </Route>
              <Route path={`${url}/roles`} exact>
                <RepositoryRoles baseUrl={`${url}/roles`} />
              </Route>
              <Route path={`${url}/roles/create`}>
                <CreateRepositoryRole />
              </Route>
              <Route path={`${url}/roles/:page`} exact>
                <RepositoryRoles baseUrl={`${url}/roles`} />
              </Route>
              <ExtensionPoint<extensionPoints.AdminRoute> name="admin.route" props={extensionProps} renderAll={true} />
            </Switch>
          </PrimaryContentColumn>
          <SecondaryNavigationColumn>
            <SecondaryNavigation label={t("admin.menu.navigationLabel")}>
              <NavLink
                to={`${url}/info`}
                icon="fas fa-info-circle"
                label={t("admin.menu.informationNavLink")}
                title={t("admin.menu.informationNavLink")}
                testId="admin-information-link"
              />
              {(availablePluginsLink || installedPluginsLink) && (
                <SubNavigation
                  to={`${url}/plugins/`}
                  icon="fas fa-puzzle-piece"
                  label={t("plugins.menu.pluginsNavLink")}
                  title={t("plugins.menu.pluginsNavLink")}
                  testId="admin-plugins-link"
                >
                  {installedPluginsLink && (
                    <NavLink
                      to={`${url}/plugins/installed/`}
                      label={t("plugins.menu.installedNavLink")}
                      testId="admin-installed-plugins-link"
                    />
                  )}
                  {availablePluginsLink && (
                    <NavLink
                      to={`${url}/plugins/available/`}
                      label={t("plugins.menu.availableNavLink")}
                      testId="admin-available-plugins-link"
                    />
                  )}
                </SubNavigation>
              )}
              <NavLink
                to={`${url}/roles/`}
                icon="fas fa-user-shield"
                label={t("repositoryRole.navLink")}
                title={t("repositoryRole.navLink")}
                testId="admin-repository-role-link"
                activeWhenMatch={matchesRoles}
                activeOnlyWhenExact={false}
              />
              <ExtensionPoint<extensionPoints.AdminNavigation>
                name="admin.navigation"
                props={extensionProps}
                renderAll={true}
              />
              <SubNavigation
                to={`${url}/settings/general`}
                label={t("admin.menu.settingsNavLink")}
                title={t("admin.menu.settingsNavLink")}
                testId="admin-settings-link"
              >
                <NavLink
                  to={`${url}/settings/general`}
                  label={t("admin.menu.generalNavLink")}
                  testId="admin-settings-general-link"
                />
                <ExtensionPoint<extensionPoints.AdminSetting>
                  name="admin.setting"
                  props={extensionProps}
                  renderAll={true}
                />
              </SubNavigation>
            </SecondaryNavigation>
          </SecondaryNavigationColumn>
        </CustomQueryFlexWrappedColumns>
      </Page>
    </StateMenuContextProvider>
  );
};

export default Admin;
