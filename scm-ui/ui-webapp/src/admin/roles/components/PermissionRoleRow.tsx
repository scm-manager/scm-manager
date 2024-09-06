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
import { Link } from "react-router-dom";
import { RepositoryRole } from "@scm-manager/ui-types";
import SystemRoleTag from "./SystemRoleTag";

type Props = {
  baseUrl: string;
  role: RepositoryRole;
};

class PermissionRoleRow extends React.Component<Props> {
  renderLink(to: string, label: string, system?: boolean) {
    return (
      <Link to={to}>
        {label} <SystemRoleTag system={system} />
      </Link>
    );
  }

  render() {
    const { baseUrl, role } = this.props;
    const singleRepoRoleUrl = baseUrl.substring(0, baseUrl.length - 1);
    const to = `${singleRepoRoleUrl}/${encodeURIComponent(role.name)}/info`;
    return (
      <tr>
        <td>{this.renderLink(to, role.name, !role._links.update)}</td>
      </tr>
    );
  }
}

export default PermissionRoleRow;
