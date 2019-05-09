// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { Role } from "@scm-manager/ui-types";

type Props = {
  role: Role
};

class PermissionRoleRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { role } = this.props;
    const to = `./${encodeURIComponent(role.name)}/info`;
    return (
      <tr>
        <td>{this.renderLink(to, role.name)}</td>
      </tr>
    );
  }
}

export default PermissionRoleRow;
