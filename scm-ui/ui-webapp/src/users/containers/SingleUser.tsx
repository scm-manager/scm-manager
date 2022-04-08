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
  StateMenuContextProvider,
  SubNavigation,
  urls
} from "@scm-manager/ui-components";
import { Details } from "./../components/table";
import EditUser from "./EditUser";
import {
  EditUserNavLink,
  SetApiKeysNavLink,
  SetPasswordNavLink,
  SetPermissionsNavLink,
  SetPublicKeysNavLink
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
    url
  };

  const escapedUrl = url.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");

  return (
    <StateMenuContextProvider>
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
            <ExtensionPoint<extensionPoints.UserRoute> name="user.route" props={extensionProps} renderAll={true} />
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
    </StateMenuContextProvider>
  );
};

export default SingleUser;
