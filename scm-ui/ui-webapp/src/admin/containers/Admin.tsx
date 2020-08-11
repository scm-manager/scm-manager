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
import { connect } from "react-redux";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { Redirect, Route, RouteComponentProps, Switch } from "react-router-dom";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Links } from "@scm-manager/ui-types";
import {
  NavLink,
  Page,
  CustomQueryFlexWrappedColumns,
  PrimaryContentColumn,
  SecondaryNavigationColumn,
  SecondaryNavigation,
  SubNavigation
} from "@scm-manager/ui-components";
import { getAvailablePluginsLink, getInstalledPluginsLink, getLinks } from "../../modules/indexResource";
import AdminDetails from "./AdminDetails";
import PluginsOverview from "../plugins/containers/PluginsOverview";
import GlobalConfig from "./GlobalConfig";
import RepositoryRoles from "../roles/containers/RepositoryRoles";
import SingleRepositoryRole from "../roles/containers/SingleRepositoryRole";
import CreateRepositoryRole from "../roles/containers/CreateRepositoryRole";
import { StateMenuContextProvider } from "@scm-manager/ui-components";

type Props = RouteComponentProps &
  WithTranslation & {
    links: Links;
    availablePluginsLink: string;
    installedPluginsLink: string;
  };

class Admin extends React.Component<Props> {
  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      if (url.includes("role")) {
        return url.substring(0, url.length - 2);
      }
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  matchesRoles = (route: any) => {
    const url = this.matchedUrl();
    const regex = new RegExp(`${url}/role/`);
    return route.location.pathname.match(regex);
  };

  render() {
    const { links, availablePluginsLink, installedPluginsLink, t } = this.props;

    const url = this.matchedUrl();
    const extensionProps = {
      links,
      url
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
                <Route
                  path={`${url}/plugins/installed`}
                  exact
                  render={() => <PluginsOverview baseUrl={`${url}/plugins/installed`} installed={true} />}
                />
                <Route
                  path={`${url}/plugins/installed/:page`}
                  exact
                  render={() => <PluginsOverview baseUrl={`${url}/plugins/installed`} installed={true} />}
                />
                <Route
                  path={`${url}/plugins/available`}
                  exact
                  render={() => <PluginsOverview baseUrl={`${url}/plugins/available`} installed={false} />}
                />
                <Route
                  path={`${url}/plugins/available/:page`}
                  exact
                  render={() => <PluginsOverview baseUrl={`${url}/plugins/available`} installed={false} />}
                />
                <Route
                  path={`${url}/role/:role`}
                  render={() => <SingleRepositoryRole baseUrl={`${url}/roles`} history={this.props.history} />}
                />
                <Route path={`${url}/roles`} exact render={() => <RepositoryRoles baseUrl={`${url}/roles`} />} />
                <Route
                  path={`${url}/roles/create`}
                  render={() => <CreateRepositoryRole history={this.props.history} />}
                />
                <Route path={`${url}/roles/:page`} exact render={() => <RepositoryRoles baseUrl={`${url}/roles`} />} />
                <ExtensionPoint name="admin.route" props={extensionProps} renderAll={true} />
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
                  activeWhenMatch={this.matchesRoles}
                  activeOnlyWhenExact={false}
                />
                <ExtensionPoint name="admin.navigation" props={extensionProps} renderAll={true} />
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
                  <ExtensionPoint name="admin.setting" props={extensionProps} renderAll={true} />
                </SubNavigation>
              </SecondaryNavigation>
            </SecondaryNavigationColumn>
          </CustomQueryFlexWrappedColumns>
        </Page>
      </StateMenuContextProvider>
    );
  }
}

const mapStateToProps = (state: any) => {
  const links = getLinks(state);
  const availablePluginsLink = getAvailablePluginsLink(state);
  const installedPluginsLink = getInstalledPluginsLink(state);
  return {
    links,
    availablePluginsLink,
    installedPluginsLink
  };
};

export default compose(connect(mapStateToProps), withTranslation("admin"))(Admin);
