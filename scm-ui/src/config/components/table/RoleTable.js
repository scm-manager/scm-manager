// @flow
import React from "react";
import { translate } from "react-i18next";
import type { Role } from "@scm-manager/ui-types";

type Props = {
  t: string => string,
  roles: Role[]
};

class RoleTable extends React.Component<Props> {
  render() {
    const { roles, t } = this.props;
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
        <tr>
          <th className="is-hidden-mobile">{t("user.name")}</th>
          <th>{t("user.displayName")}</th>
          <th>{t("user.mail")}</th>
          <th className="is-hidden-mobile">{t("user.active")}</th>
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

export default translate("config")(RoleTable);
