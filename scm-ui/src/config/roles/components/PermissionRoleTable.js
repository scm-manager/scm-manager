// @flow
import React from "react";
import { translate } from "react-i18next";
import type { RepositoryRole } from "@scm-manager/ui-types";
import PermissionRoleRow from "./PermissionRoleRow";

type Props = {
  baseUrl: string,
  roles: RepositoryRole[],

  // context props
  t: string => string
};

class PermissionRoleTable extends React.Component<Props> {
  render() {
    const { baseUrl, roles, t } = this.props;
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
        <tr>
          <th>{t("repositoryRole.name")}</th>
        </tr>
        </thead>
        <tbody>
        {roles.map((role, index) => {
          return (
            <PermissionRoleRow key={index} baseUrl={baseUrl} role={role} />
          );
        })}
        </tbody>
      </table>
    );
  }
}

export default translate("config")(PermissionRoleTable);
