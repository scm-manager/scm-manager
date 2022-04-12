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
import { useTranslation } from "react-i18next";
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
import { EditGroupNavLink, SetPermissionsNavLink } from "./../components/navLinks";
import EditGroup from "./EditGroup";
import { useGroup } from "@scm-manager/ui-api";
import SetGroupPermissions from "../../permissions/components/SetGroupPermissions";

const SingleGroup: FC = () => {
  const { name } = useParams<{ name: string }>();
  const match = useRouteMatch();
  const { data: group, isLoading, error } = useGroup(name);
  const [t] = useTranslation("groups");

  if (error) {
    return <ErrorPage title={t("singleGroup.errorTitle")} subtitle={t("singleGroup.errorSubtitle")} error={error} />;
  }

  if (!group || isLoading) {
    return <Loading />;
  }

  const url = urls.matchedUrlFromMatch(match);
  const escapedUrl = urls.escapeUrlForRoute(url);

  const extensionProps = {
    group,
    url: escapedUrl
  };

  return (
    <StateMenuContextProvider>
      <Page title={group.name}>
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Route path={escapedUrl} exact>
              <Details group={group} />
            </Route>
            <Route path={`${escapedUrl}/settings/general`} exact>
              <EditGroup group={group} />
            </Route>
            <Route path={`${escapedUrl}/settings/permissions`} exact>
              <SetGroupPermissions group={group} />
            </Route>
            <ExtensionPoint<extensionPoints.GroupRoute> name="group.route" props={extensionProps} renderAll={true} />
          </PrimaryContentColumn>
          <SecondaryNavigationColumn>
            <SecondaryNavigation label={t("singleGroup.menu.navigationLabel")}>
              <NavLink
                to={`${url}`}
                icon="fas fa-info-circle"
                label={t("singleGroup.menu.informationNavLink")}
                title={t("singleGroup.menu.informationNavLink")}
              />
              <ExtensionPoint<extensionPoints.GroupNavigation>
                name="group.navigation"
                props={extensionProps}
                renderAll={true}
              />
              <SubNavigation
                to={`${url}/settings/general`}
                label={t("singleGroup.menu.settingsNavLink")}
                title={t("singleGroup.menu.settingsNavLink")}
              >
                <EditGroupNavLink group={group} editUrl={`${url}/settings/general`} />
                <SetPermissionsNavLink group={group} permissionsUrl={`${url}/settings/permissions`} />
                <ExtensionPoint<extensionPoints.GroupSetting>
                  name="group.setting"
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

export default SingleGroup;
