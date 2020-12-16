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
import { Redirect, Route, RouteComponentProps, Switch, withRouter } from "react-router-dom";
import { getMe } from "../modules/auth";
import { compose } from "redux";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { Me } from "@scm-manager/ui-types";
import {
  ErrorPage,
  NavLink,
  Page,
  CustomQueryFlexWrappedColumns,
  PrimaryContentColumn,
  SecondaryNavigationColumn,
  SecondaryNavigation,
  SubNavigation,
  StateMenuContextProvider,
  urls
} from "@scm-manager/ui-components";
import ChangeUserPassword from "./ChangeUserPassword";
import ProfileInfo from "./ProfileInfo";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import SetPublicKeys from "../users/components/publicKeys/SetPublicKeys";
import SetPublicKeysNavLink from "../users/components/navLinks/SetPublicKeysNavLink";
import SetApiKeys from "../users/components/apiKeys/SetApiKeys";
import SetApiKeysNavLink from "../users/components/navLinks/SetApiKeysNavLink";

type Props = RouteComponentProps &
  WithTranslation & {
    me: Me;

    // Context props
    match: any;
  };

class Profile extends React.Component<Props> {
  mayChangePassword = () => !!this.props.me?._links?.password;
  canManagePublicKeys = () => !!this.props.me?._links?.publicKeys;
  canManageApiKeys = () => !!this.props.me?._links?.apiKeys;

  shouldRenderNavigation = () => {
    const { me } = this.props;
    return (
      !!me?._links?.password ||
      !!me?._links?.publicKeys ||
      !!me?._links?.apiKeys ||
      binder.hasExtension("profile.route")
    );
  };

  render() {
    const url = urls.matchedUrl(this.props);
    const { me, t } = this.props;

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
      <StateMenuContextProvider>
        <Page title={me.displayName}>
          <CustomQueryFlexWrappedColumns>
            <PrimaryContentColumn>
              <Route path={url} exact render={() => <ProfileInfo me={me} />} />
              {this.shouldRenderNavigation() && (
                <Switch>
                  {this.mayChangePassword() && (
                    <Redirect exact from={`${url}/settings/`} to={`${url}/settings/password`} />
                  )}
                  {this.canManagePublicKeys() && (
                    <Redirect exact from={`${url}/settings/`} to={`${url}/settings/publicKeys`} />
                  )}
                  {this.canManageApiKeys() && (
                    <Redirect exact from={`${url}/settings/`} to={`${url}/settings/apiKeys`} />
                  )}
                </Switch>
              )}
              {this.mayChangePassword() && (
                <Route path={`${url}/settings/password`} render={() => <ChangeUserPassword me={me} />} />
              )}
              {this.canManagePublicKeys() && (
                <Route path={`${url}/settings/publicKeys`} render={() => <SetPublicKeys user={me} />} />
              )}
              {this.canManageApiKeys() && (
                <Route path={`${url}/settings/apiKeys`} render={() => <SetApiKeys user={me} />} />
              )}
              <ExtensionPoint name="profile.route" props={extensionProps} renderAll={true} />
            </PrimaryContentColumn>
            <SecondaryNavigationColumn>
              <SecondaryNavigation label={t("profile.navigationLabel")}>
                <NavLink
                  to={`${url}`}
                  icon="fas fa-info-circle"
                  label={t("profile.informationNavLink")}
                  title={t("profile.informationNavLink")}
                />
                {this.shouldRenderNavigation() && (
                  <SubNavigation
                    to={`${url}/settings/`}
                    label={t("profile.settingsNavLink")}
                    title={t("profile.settingsNavLink")}
                  >
                    {this.mayChangePassword() && (
                      <NavLink to={`${url}/settings/password`} label={t("profile.changePasswordNavLink")} />
                    )}
                    <SetPublicKeysNavLink user={me} publicKeyUrl={`${url}/settings/publicKeys`} />
                    <SetApiKeysNavLink user={me} apiKeyUrl={`${url}/settings/apiKeys`} />
                    <ExtensionPoint name="profile.setting" props={extensionProps} renderAll={true} />
                  </SubNavigation>
                )}
              </SecondaryNavigation>
            </SecondaryNavigationColumn>
          </CustomQueryFlexWrappedColumns>
        </Page>
      </StateMenuContextProvider>
    );
  }
}

const mapStateToProps = (state: any) => {
  return {
    me: getMe(state)
  };
};

export default compose(withTranslation("commons"), connect(mapStateToProps), withRouter)(Profile);
