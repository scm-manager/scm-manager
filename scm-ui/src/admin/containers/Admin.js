// @flow
import React from "react";
import { translate } from "react-i18next";
import {Redirect, Route, Switch} from "react-router-dom";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { History } from "history";
import { connect } from "react-redux";
import { compose } from "redux";
import type { Links } from "@scm-manager/ui-types";
import { Page, Navigation, NavLink, Section, SubNavigation } from "@scm-manager/ui-components";
import { getLinks } from "../../modules/indexResource";
import AdminDetails from "./AdminDetails";
import PluginsOverview from "../plugins/containers/PluginsOverview";
import GlobalConfig from "./GlobalConfig";
import RepositoryRoles from "../roles/containers/RepositoryRoles";
import SingleRepositoryRole from "../roles/containers/SingleRepositoryRole";
import CreateRepositoryRole from "../roles/containers/CreateRepositoryRole";

type Props = {
  links: Links,

  // context objects
  t: string => string,
  match: any,
  history: History
};

class Admin extends React.Component<Props> {
  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      if(url.includes("role")) {
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
    const { links, t } = this.props;

    const url = this.matchedUrl();
    const extensionProps = {
      links,
      url
    };

    return (
      <Page>
        <div className="columns">
          <div className="column is-three-quarters">
            <Switch>
              <Redirect exact from={url} to={`${url}/info`} />
              <Route path={`${url}/info`} exact component={AdminDetails} />
              <Route path={`${url}/settings/general`} exact component={GlobalConfig} />
              <Redirect exact from={`${url}/plugins`} to={`${url}/plugins/installed/`} />
              <Route
                path={`${url}/plugins/installed`}
                exact
                render={() => (
                  <PluginsOverview baseUrl={`${url}/plugins/installed`} installed={true} />
                )}
              />
              <Route
                path={`${url}/plugins/installed/:page`}
                exact
                render={() => (
                  <PluginsOverview baseUrl={`${url}/plugins/installed`} installed={true} />
                )}
              />
              <Route
                path={`${url}/plugins/available`}
                exact
                render={() => (
                  <PluginsOverview baseUrl={`${url}/plugins/available`} installed={false} />
                )}
              />
              <Route
                path={`${url}/plugins/available/:page`}
                exact
                render={() => (
                  <PluginsOverview baseUrl={`${url}/plugins/available`} installed={false} />
                )}
              />
              <Route
                path={`${url}/role/:role`}
                render={() => (
                  <SingleRepositoryRole
                    baseUrl={`${url}/roles`}
                    history={this.props.history}
                  />
                )}
              />
              <Route
                path={`${url}/roles`}
                exact
                render={() => <RepositoryRoles baseUrl={`${url}/roles`} />}
              />
              <Route
                path={`${url}/roles/create`}
                render={() => (
                  <CreateRepositoryRole
                    history={this.props.history}
                  />
                )}
              />
              <Route
                path={`${url}/roles/:page`}
                exact
                render={() => (
                  <RepositoryRoles baseUrl={`${url}/roles`} />
                )}
              />
              <ExtensionPoint
                name="admin.route"
                props={extensionProps}
                renderAll={true}
              />
            </Switch>
          </div>
          <div className="column is-one-quarter">
            <Navigation>
              <Section label={t("admin.menu.navigationLabel")}>
                <NavLink
                  to={`${url}/info`}
                  icon="fas fa-info-circle"
                  label={t("admin.menu.informationNavLink")}
                />
                <SubNavigation
                  to={`${url}/plugins/`}
                  icon="fas fa-puzzle-piece"
                  label={t("plugins.menu.pluginsNavLink")}
                >
                  <NavLink
                    to={`${url}/plugins/installed/`}
                    label={t("plugins.menu.installedNavLink")}
                  />
                  {/* Activate this again after available plugins page is created */}
                  {/*<NavLink*/}
                  {/*  to={`${url}/plugins/available/`}*/}
                  {/*  label={t("plugins.menu.availableNavLink")}*/}
                  {/*/>*/}
                </SubNavigation>
                <NavLink
                  to={`${url}/roles/`}
                  icon="fas fa-user-shield"
                  label={t("repositoryRole.navLink")}
                  activeWhenMatch={this.matchesRoles}
                  activeOnlyWhenExact={false}
                />
                <ExtensionPoint
                  name="admin.navigation"
                  props={extensionProps}
                  renderAll={true}
                />
                <SubNavigation
                  to={`${url}/settings/general`}
                  label={t("admin.menu.settingsNavLink")}
                >
                  <NavLink
                    to={`${url}/settings/general`}
                    label={t("admin.menu.generalNavLink")}
                  />
                  <ExtensionPoint
                    name="admin.setting"
                    props={extensionProps}
                    renderAll={true}
                  />
                </SubNavigation>
              </Section>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = (state: any) => {
  const links = getLinks(state);
  return {
    links
  };
};

export default compose(
  connect(mapStateToProps),
  translate("admin")
)(Admin);
