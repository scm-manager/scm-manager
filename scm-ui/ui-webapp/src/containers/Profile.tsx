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
import { Route, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  StateMenuContextProvider,
  SubNavigation,
  urls
} from "@scm-manager/ui-components";
import ChangeUserPassword from "./ChangeUserPassword";
import ProfileInfo from "./ProfileInfo";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import SetPublicKeys from "../users/components/publicKeys/SetPublicKeys";
import SetPublicKeysNavLink from "../users/components/navLinks/SetPublicKeysNavLink";
import SetApiKeys from "../users/components/apiKeys/SetApiKeys";
import SetApiKeysNavLink from "../users/components/navLinks/SetApiKeysNavLink";
import { useRequiredMe } from "@scm-manager/ui-api";
import Theme from "./Theme";

const Profile: FC = () => {
  const match = useRouteMatch();
  const url = urls.matchedUrlFromMatch(match);
  const [t] = useTranslation("commons");
  const me = useRequiredMe();

  const mayChangePassword = !!me._links.password;
  const canManagePublicKeys = !!me._links.publicKeys;
  const canManageApiKeys = !!me._links.apiKeys;

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
            <Route path={url} exact>
              <ProfileInfo me={me} />
            </Route>
            <Route path={`${url}/settings/theme`} exact>
              <Theme />
            </Route>
            {mayChangePassword && (
              <Route path={`${url}/settings/password`}>
                <ChangeUserPassword me={me} />
              </Route>
            )}
            {canManagePublicKeys && (
              <Route path={`${url}/settings/publicKeys`}>
                <SetPublicKeys user={me} />
              </Route>
            )}
            {canManageApiKeys && (
              <Route path={`${url}/settings/apiKeys`}>
                <SetApiKeys user={me} />
              </Route>
            )}
            <ExtensionPoint<extensionPoints.ProfileRoute>
              name="profile.route"
              props={extensionProps}
              renderAll={true}
            />
          </PrimaryContentColumn>
          <SecondaryNavigationColumn>
            <SecondaryNavigation label={t("profile.navigationLabel")}>
              <NavLink
                to={url}
                icon="fas fa-info-circle"
                label={t("profile.informationNavLink")}
                title={t("profile.informationNavLink")}
              />
              <SubNavigation
                to={`${url}/settings/theme`}
                label={t("profile.settingsNavLink")}
                title={t("profile.settingsNavLink")}
              >
                <NavLink
                  to={`${url}/settings/theme`}
                  icon="fas fa-palette"
                  label={t("profile.theme.nav.label")}
                  title={t("profile.theme.nav.title")}
                />
                {mayChangePassword && (
                  <NavLink to={`${url}/settings/password`} label={t("profile.changePasswordNavLink")} />
                )}
                <SetPublicKeysNavLink user={me} publicKeyUrl={`${url}/settings/publicKeys`} />
                <SetApiKeysNavLink user={me} apiKeyUrl={`${url}/settings/apiKeys`} />
                {/* TODO: ProfileSettingExtension props are not */}
                <ExtensionPoint<extensionPoints.ProfileSetting>
                  name="profile.setting"
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

export default Profile;
