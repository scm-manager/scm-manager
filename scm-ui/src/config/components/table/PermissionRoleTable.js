// @flow
import React from "react";
import { translate } from "react-i18next";
import type { Role } from "@scm-manager/ui-types";

type Props = {
  t: string => string,
  roles: Role[]
};

class PermissionRoleTable extends React.Component<Props> {
  render() {
    const { roles, t } = this.props;
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
        <tr>
          <th>{t("role.form.name")}</th>
          <th>{t("role.form.permissions")}</th>
        </tr>
        </thead>
        <tbody>
        {roles.map((role, index) => {
          return <p key={index}>role</p>;
        })}
        </tbody>
      </table>
    );
  }
}

export default translate("config")(PermissionRoleTable);
