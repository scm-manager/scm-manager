// @flow
import React from "react";
import { translate } from "react-i18next";
import { Route } from "react-router";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { History } from "history";
import { connect } from "react-redux";
import { compose } from "redux";
import type { Links } from "@scm-manager/ui-types";
import { Page, Navigation, NavLink, Section } from "@scm-manager/ui-components";
import { getLinks } from "../../modules/indexResource";
import GlobalConfig from "./GlobalConfig";
import RepositoryRoles from "../roles/containers/RepositoryRoles";
import SingleRepositoryRole from "../roles/containers/SingleRepositoryRole";

type Props = {
  links: Links,

  // context objects
  t: string => string,
  match: any,
  history: History
};

class Config extends React.Component<Props> {
  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
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
            <Route path={url} exact component={GlobalConfig} />
            <Route
              path={`${url}/role/:role`}
              render={() => <SingleRepositoryRole baseUrl={`${url}/roles`} />}
            />
            <Route
              path={`${url}/roles`}
              exact
              render={() => <RepositoryRoles baseUrl={`${url}/roles`} />}
            />
            <Route
              path={`${url}/roles/create`}
              render={() => <CreatePermissionRole />}
            />
            <ExtensionPoint
              name="config.route"
              props={extensionProps}
              renderAll={true}
            />
          </div>
          <div className="column is-one-quarter">
            <Navigation>
              <Section label={t("config.navigationLabel")}>
                <NavLink
                  to={`${url}`}
                  label={t("config.globalConfigurationNavLink")}
                />
                <NavLink
                  to={`${url}/roles`}
                  label={t("roles.navLink")}
                  activeWhenMatch={this.matchesRoles}
                />
                <ExtensionPoint
                  name="config.navigation"
                  props={extensionProps}
                  renderAll={true}
                />
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
  translate("config")
)(Config);
