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
import { useTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import { Help, InfoTable, MailLink } from "@scm-manager/ui-components";
import PermissionOverview from "../PermissionOverview";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { DateFromNow, Icon, useDocumentTitle, createAttributesForTesting } from "@scm-manager/ui-core";
import BooleanIcon from "../../../components/BooleanIcon";

type Props = {
  user: User;
};

const Details: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  useDocumentTitle(t("singleUser.menu.informationNavLink"), user.displayName);
  const [collapsed, setCollapsed] = useState(true);
  const toggleCollapse = () => setCollapsed(!collapsed);

  let permissionOverview;
  if (user._links.permissionOverview) {
    permissionOverview = (
      <div className="content">
        <h3 className="is-clickable" onClick={toggleCollapse}>
          <Icon>{collapsed ? "angle-right" : "angle-down"}</Icon> {t("permissionOverview.title")}{" "}
          <Help message={t("permissionOverview.help")} />
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
              <BooleanIcon value={user.active} />
            </td>
          </tr>
          <tr>
            <th>{t("user.externalFlag")}</th>
            <td>
              <BooleanIcon value={user.external} />
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
          <ExtensionPoint<extensionPoints.UserInformationTableBottom>
            name="user.information.table.bottom"
            props={{ user }}
            renderAll={true}
          />
        </tbody>
      </InfoTable>
      {permissionOverview}
    </>
  );
};

export default Details;
