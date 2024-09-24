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
  SubNavigation,
  urls,
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
import Accessibility from "./Accessibility";

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
          message: t("profile.error-message"),
        }}
      />
    );
  }

  const extensionProps = {
    me,
    url,
  };

  return (
    <Page title={me.displayName}>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>
          <Route path={url} exact>
            <ProfileInfo me={me} />
          </Route>
          <Route path={`${url}/settings/theme`} exact>
            <Theme />
          </Route>
          <Route path={`${url}/settings/accessibility`} exact>
            <Accessibility />
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
          <ExtensionPoint<extensionPoints.ProfileRoute> name="profile.route" props={extensionProps} renderAll={true} />
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
              <NavLink to={`${url}/settings/theme`} label={t("profile.theme.navLink")} />
              <NavLink to={`${url}/settings/accessibility`} label={t("profile.accessibility.navLink")} />
              {mayChangePassword && (
                <NavLink to={`${url}/settings/password`} label={t("profile.changePasswordNavLink")} />
              )}
              <SetPublicKeysNavLink user={me} publicKeyUrl={`${url}/settings/publicKeys`} />
              <SetApiKeysNavLink user={me} apiKeyUrl={`${url}/settings/apiKeys`} />
              <ExtensionPoint name="profile.setting" props={extensionProps} renderAll={true} />
            </SubNavigation>
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default Profile;
