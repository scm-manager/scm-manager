// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { Role } from "@scm-manager/ui-types";
import SystemRoleTag from "../SystemRoleTag";

type Props = {
  baseUrl: string,
  role: Role
};

class PermissionRoleRow extends React.Component<Props> {
  renderLink(to: string, label: string, system?: boolean) {
    if (!system) {
      return <Link to={to}>{label}</Link>;
    }
    return (
      <>
        {label} <SystemRoleTag system={system} />
      </>
    );
  }

  render() {
    const { baseUrl, role } = this.props;
    const to = `${baseUrl}/${encodeURIComponent(role.name)}/edit`;
    return (
      <tr>
        <td>{this.renderLink(to, role.name, !role._links.update)}</td>
      </tr>
    );
  }
}

export default PermissionRoleRow;
