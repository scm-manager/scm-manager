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
import { Route, RouteComponentProps, withRouter } from "react-router-dom";
import { getMe } from "../modules/auth";
import { compose } from "redux";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { Me } from "@scm-manager/ui-types";
import {
  ErrorPage,
  isMenuCollapsed,
  MenuContext,
  NavLink,
  Page,
  SecondaryNavigation,
  SubNavigation
} from "@scm-manager/ui-components";
import ChangeUserPassword from "./ChangeUserPassword";
import ProfileInfo from "./ProfileInfo";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { storeMenuCollapsed } from "@scm-manager/ui-components/src";

type Props = RouteComponentProps &
  WithTranslation & {
    me: Me;

    // Context props
    match: any;
  };

type State = {
  menuCollapsed: boolean;
};

class Profile extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      menuCollapsed: isMenuCollapsed()
    };
  }

  onCollapseProfileMenu = (collapsed: boolean) => {
    this.setState({ menuCollapsed: collapsed }, () => storeMenuCollapsed(collapsed));
  };

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
    const url = this.matchedUrl();

    const { me, t } = this.props;
    const { menuCollapsed } = this.state;

    if (!me) {
      return (
        <ErrorPage
          title={t("profile.error-title")}
          subtitle={t("profile.error-subtitle")}
          error={{
            name: t("profile.error"),
            message: t("profile.error-message")
          }}
        />
      );
    }

    const extensionProps = {
      me,
      url
    };

    return (
      <MenuContext.Provider
        value={{ menuCollapsed, setMenuCollapsed: (collapsed: boolean) => this.setState({ menuCollapsed: collapsed }) }}
      >
        <Page title={me.displayName}>
          <div className="columns">
            <div className="column">
              <Route path={url} exact render={() => <ProfileInfo me={me} />} />
              <Route path={`${url}/settings/password`} render={() => <ChangeUserPassword me={me} />} />
              <ExtensionPoint name="profile.route" props={extensionProps} renderAll={true} />
            </div>
            <div className={menuCollapsed ? "column is-1" : "column is-3"}>
              <SecondaryNavigation
                label={t("profile.navigationLabel")}
                onCollapse={() => this.onCollapseProfileMenu(!menuCollapsed)}
                collapsed={menuCollapsed}
              >
                <NavLink
                  to={`${url}`}
                  icon="fas fa-info-circle"
                  label={t("profile.informationNavLink")}
                  title={t("profile.informationNavLink")}
                />
                <SubNavigation
                  to={`${url}/settings/password`}
                  label={t("profile.settingsNavLink")}
                  title={t("profile.settingsNavLink")}
                >
                  <NavLink to={`${url}/settings/password`} label={t("profile.changePasswordNavLink")} />
                  <ExtensionPoint name="profile.setting" props={extensionProps} renderAll={true} />
                </SubNavigation>
              </SecondaryNavigation>
            </div>
          </div>
        </Page>
      </MenuContext.Provider>
    );
  }
}

const mapStateToProps = (state: any) => {
  return {
    me: getMe(state)
  };
};

export default compose(withTranslation("commons"), connect(mapStateToProps), withRouter)(Profile);
