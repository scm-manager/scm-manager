// @flow
import React from "react";
import { translate } from "react-i18next";
import { Route } from "react-router";

import {
  Page,
  Navigation,
  NavLink,
  Section,
  Help
} from "@scm-manager/ui-components";
import GlobalConfig from "./GlobalConfig";
import type { History } from "history";

type Props = {
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
    const { t } = this.props;

    const url = this.matchedUrl();

    return (
      <Page>
        <Help message={"Hallo"} />
        <div className="columns">
          <div className="column is-three-quarters">
            <Route path={url} exact component={GlobalConfig} />
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("config.navigation-title")}>
                <NavLink
                  to={`${url}`}
                  label={t("global-config.navigation-label")}
                />
              </Section>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

export default translate("config")(Config);
