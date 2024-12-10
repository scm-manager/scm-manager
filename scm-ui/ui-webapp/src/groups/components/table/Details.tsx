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
import { useTranslation } from "react-i18next";
import { Group } from "@scm-manager/ui-types";
import { useDocumentTitle } from "@scm-manager/ui-core";
import { Checkbox, DateFromNow, InfoTable } from "@scm-manager/ui-components";
import GroupMember from "./GroupMember";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";

type Props = {
  group: Group;
};

const Details: FC<Props> = ({ group }) => {
  const [t] = useTranslation("groups");
  useDocumentTitle(t("singleGroup.menu.informationNavLink"), group.name);

  const renderMembers = () => {
    let member = null;
    if (group.members.length > 0) {
      member = (
        <tr>
          <th>{t("group.members")}</th>
          <td className="p-0">
            <ul className="ml-4">
              {group._embedded.members.map((member, index) => {
                return <GroupMember key={index} member={member} />;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return member;
  };

  return (
    <InfoTable className="content">
      <tbody>
        <tr>
          <th>{t("group.name")}</th>
          <td>{group.name}</td>
        </tr>
        <tr>
          <th>{t("group.description")}</th>
          <td>{group.description}</td>
        </tr>
        <tr>
          <th>{t("group.external")}</th>
          <td>
            <Checkbox checked={group.external} readOnly={true} />
          </td>
        </tr>
        <tr>
          <th>{t("group.type")}</th>
          <td>{group.type}</td>
        </tr>
        <tr>
          <th>{t("group.creationDate")}</th>
          <td>
            <DateFromNow date={group.creationDate} />
          </td>
        </tr>
        <tr>
          <th>{t("group.lastModified")}</th>
          <td>
            <DateFromNow date={group.lastModified} />
          </td>
        </tr>
        {renderMembers()}
        <ExtensionPoint<extensionPoints.GroupInformationTableBottom>
          name="group.information.table.bottom"
          props={{ group }}
          renderAll={true}
        />
      </tbody>
    </InfoTable>
  );
};

export default Details;
