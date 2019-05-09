// @flow
import React from "react";
import { translate } from "react-i18next";
import { Route } from "react-router";
import { ExtensionPoint } from "@scm-manager/ui-extensions";

import type { Links } from "@scm-manager/ui-types";
import { Page, Navigation, NavLink, Section } from "@scm-manager/ui-components";
import GlobalConfig from "./GlobalConfig";
import GlobalPermissionRoles from "./GlobalPermissionRoles";
import type { History } from "history";
import { connect } from "react-redux";
import { compose } from "redux";
import { getLinks } from "../../modules/indexResource";

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
              path={`${url}/roles`}
              exact
              render={() => (
                <GlobalPermissionRoles
                  baseUrl={`${url}/roles`}
                />
              )}
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
