// @flow
import React from "react";
import { translate } from "react-i18next";
import PermissionRow from "../components/table/PermissionRow";
import type { Permission, PermissionCollection } from "../types/Permissions";
import SinglePermission from "./SinglePermission";
import type { History } from "history";

type Props = {
  t: string => string,
  permissions: PermissionCollection,
  modifyPermission: (Permission, string, string, () => void) => void,
  namespace: string,
  name: string,
  match: any,
  history: History
};

class PermissionsTable extends React.Component<Props> {
  permissionsModified = () => {
    const { history, name, namespace } = this.props;
    console.log(history);
    history.push(`/repo/${namespace}/${name}/permissions`);
  };

  render() {
    const { permissions, t, namespace, name } = this.props;

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
            if (permission._links.update)
              return (
                <SinglePermission
                  key={index}
                  namespace={namespace}
                  name={name}
                  permission={permission}
                />
              );
            else return <PermissionRow key={index} permission={permission} />;
          })}
        </tbody>
      </table>
    );
  }
}

export default translate("permissions")(PermissionsTable);
