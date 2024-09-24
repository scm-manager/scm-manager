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
import { Route, useParams, useRouteMatch } from "react-router-dom";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  Loading,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  SubNavigation,
  urls,
} from "@scm-manager/ui-components";
import { Details } from "./../components/table";
import EditUser from "./EditUser";
import {
  EditUserNavLink,
  SetApiKeysNavLink,
  SetPasswordNavLink,
  SetPermissionsNavLink,
  SetPublicKeysNavLink,
} from "./../components/navLinks";
import { useTranslation } from "react-i18next";
import SetUserPassword from "../components/SetUserPassword";
import SetPublicKeys from "../components/publicKeys/SetPublicKeys";
import SetApiKeys from "../components/apiKeys/SetApiKeys";
import { useUser } from "@scm-manager/ui-api";
import SetUserPermissions from "../../permissions/components/SetUserPermissions";

const SingleUser: FC = () => {
  const [t] = useTranslation("users");
  const { name } = useParams<{ name: string }>();
  const match = useRouteMatch();
  const { isLoading, data: user, error } = useUser(name);

  if (error) {
    return <ErrorPage title={t("singleUser.errorTitle")} subtitle={t("singleUser.errorSubtitle")} error={error} />;
  }

  if (!user || isLoading) {
    return <Loading />;
  }

  const url = urls.matchedUrlFromMatch(match);

  const extensionProps = {
    user,
    url,
  };

  const escapedUrl = urls.escapeUrlForRoute(url);

  return (
    <Page title={user.displayName}>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>
          <Route path={escapedUrl} exact>
            <Details user={user} />
          </Route>
          <Route path={`${escapedUrl}/settings/general`}>
            <EditUser user={user} />
          </Route>
          <Route path={`${escapedUrl}/settings/password`}>
            <SetUserPassword user={user} />
          </Route>
          <Route path={`${escapedUrl}/settings/permissions`}>
            <SetUserPermissions user={user} />
          </Route>
          <Route path={`${escapedUrl}/settings/publickeys`}>
            <SetPublicKeys user={user} />
          </Route>
          <Route path={`${escapedUrl}/settings/apiKeys`}>
            <SetApiKeys user={user} />
          </Route>
          <ExtensionPoint<extensionPoints.UserRoute>
            name="user.route"
            props={{
              user,
              url: escapedUrl,
            }}
            renderAll={true}
          />
        </PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("singleUser.menu.navigationLabel")}>
            <NavLink
              to={url}
              icon="fas fa-info-circle"
              label={t("singleUser.menu.informationNavLink")}
              title={t("singleUser.menu.informationNavLink")}
              testId="user-information-link"
            />
            <SubNavigation
              to={`${url}/settings/general`}
              label={t("singleUser.menu.settingsNavLink")}
              title={t("singleUser.menu.settingsNavLink")}
              testId="user-settings-link"
            >
              <EditUserNavLink user={user} editUrl={`${url}/settings/general`} />
              <SetPasswordNavLink user={user} passwordUrl={`${url}/settings/password`} />
              <SetPermissionsNavLink user={user} permissionsUrl={`${url}/settings/permissions`} />
              <SetPublicKeysNavLink user={user} publicKeyUrl={`${url}/settings/publickeys`} />
              <SetApiKeysNavLink user={user} apiKeyUrl={`${url}/settings/apiKeys`} />
              <ExtensionPoint<extensionPoints.UserSetting>
                name="user.setting"
                props={extensionProps}
                renderAll={true}
              />
            </SubNavigation>
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default SingleUser;
