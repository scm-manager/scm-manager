// @flow
import React from "react";
import { translate } from "react-i18next";
import PermissionRow from "./PermissionRow";
import type { PermissionCollection } from "../../types/Permissions";

type Props = {
  t: string => string,
  permissions: PermissionCollection
};

class PermissionsTable extends React.Component<Props> {
  render() {
    const { permissions, t } = this.props;
    return (
      <table className="table is-hoverable is-fullwidth">
        <thead>
        <tr>
          <th>{t("permission.name")}</th>
          <th className="is-hidden-mobile">{t("permission.type")}</th>
          <th>{t("permission.group-permission")}</th>
        </tr>
        </thead>
        <tbody>
        {permissions.map((permission, index) => {
          return <PermissionRow key={index} permission={permission} />;
        })}
        </tbody>
      </table>
    );
  }
}

export default translate("permissions")(PermissionsTable);
