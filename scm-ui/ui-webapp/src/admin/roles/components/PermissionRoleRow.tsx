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
