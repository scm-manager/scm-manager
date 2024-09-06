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
  SubNavigation,
  urls,
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
    url,
  };

  return (
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
          <ExtensionPoint<extensionPoints.GroupRoute>
            name="group.route"
            props={{
              group,
              url: escapedUrl,
            }}
            renderAll={true}
          />
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
  );
};

export default SingleGroup;
