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

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";
import AvailableVerbs from "./AvailableVerbs";
import { InfoTable } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  role: RepositoryRole;
};

class PermissionRoleDetailsTable extends React.Component<Props> {
  render() {
    const { role, t } = this.props;
    return (
      <InfoTable className="content">
        <tbody>
          <tr>
            <th>{t("repositoryRole.name")}</th>
            <td>{role.name}</td>
          </tr>
          <tr>
            <th>{t("repositoryRole.type")}</th>
            <td>{role.type}</td>
          </tr>
          <tr>
            <th>{t("repositoryRole.verbs")}</th>
            <AvailableVerbs role={role} />
          </tr>
        </tbody>
      </InfoTable>
    );
  }
}

export default withTranslation("admin")(PermissionRoleDetailsTable);
