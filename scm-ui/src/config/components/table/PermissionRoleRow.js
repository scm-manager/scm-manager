// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { Role } from "@scm-manager/ui-types";

type Props = {
  baseUrl: string,
  role: Role
};

class PermissionRoleRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { baseUrl, role } = this.props;
    const to = `${baseUrl}/${encodeURIComponent(role.name)}/edit`;
    return (
      <tr>
        <td>{this.renderLink(to, role.name)}</td>
      </tr>
    );
  }
}

export default PermissionRoleRow;
