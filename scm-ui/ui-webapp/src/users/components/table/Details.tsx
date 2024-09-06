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

import React, { FC, useState } from "react";
import { useTranslation, WithTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import {
  Checkbox,
  createAttributesForTesting,
  DateFromNow,
  Help,
  InfoTable,
  MailLink
} from "@scm-manager/ui-components";
import { Icon } from "@scm-manager/ui-components";
import PermissionOverview from "../PermissionOverview";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";

type Props = WithTranslation & {
  user: User;
};

const Details: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const [collapsed, setCollapsed] = useState(true);
  const toggleCollapse = () => setCollapsed(!collapsed);

  let permissionOverview;
  if (user._links.permissionOverview) {
    let icon = <Icon name="angle-right" color="inherit" alt={t("diff.showContent")} />;
    if (!collapsed) {
      icon = <Icon name="angle-down" color="inherit" alt={t("diff.hideContent")} />;
    }
    permissionOverview = (
      <div className="content">
        <h3 className="is-clickable" onClick={toggleCollapse}>
          {icon} {t("permissionOverview.title")} <Help message={t("permissionOverview.help")} />
        </h3>
        {!collapsed && <PermissionOverview user={user} />}
      </div>
    );
  }

  return (
    <>
      <InfoTable>
        <tbody>
          <tr>
            <th>{t("user.name")}</th>
            <td {...createAttributesForTesting(user.name)}>{user.name}</td>
          </tr>
          <tr>
            <th>{t("user.displayName")}</th>
            <td {...createAttributesForTesting(user.displayName)}>{user.displayName}</td>
          </tr>
          <tr>
            <th>{t("user.mail")}</th>
            <td>
              <MailLink address={user.mail} />
            </td>
          </tr>
          <tr>
            <th>{t("user.active")}</th>
            <td>
              <Checkbox checked={user.active} readOnly={true} />
            </td>
          </tr>
          <tr>
            <th>{t("user.externalFlag")}</th>
            <td>
              <Checkbox checked={user.external} readOnly={true} />
            </td>
          </tr>
          <tr>
            <th>{t("user.creationDate")}</th>
            <td>
              <DateFromNow date={user.creationDate} />
            </td>
          </tr>
          <tr>
            <th>{t("user.lastModified")}</th>
            <td>
              <DateFromNow date={user.lastModified} />
            </td>
          </tr>
          <ExtensionPoint<extensionPoints.UserInformationTableBottom> name="user.information.table.bottom" props={{user}} renderAll={true} />
        </tbody>
      </InfoTable>
      {permissionOverview}
    </>
  );
};

export default Details;
