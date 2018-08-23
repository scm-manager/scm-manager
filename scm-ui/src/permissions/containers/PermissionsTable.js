// @flow
import React from "react";
import { translate } from "react-i18next";
import PermissionRow from "../components/table/PermissionRow";
import type { Permission, PermissionCollection } from "../types/Permissions";
import PermissionRowEditable from "../components/table/PermissionRowEditable";
import connect from "react-redux/es/connect/connect";
import { modifyPermission } from "../modules/permissions";
import type { History } from "history";
import {
  getModifyRepoFailure,
  isModifyRepoPending
} from "../../repos/modules/repos";

type Props = {
  t: string => string,
  permissions: PermissionCollection,
  modifyPermission: (Permission, string, string, () => void) => void,
  namespace: string,
  name: string,
  history: History
};

class PermissionsTable extends React.Component<Props> {
  permissionsModified = () => {
    const { history, name, namespace } = this.props;
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
                <PermissionRowEditable
                  key={index}
                  permission={permission}
                  submitForm={permission => {
                    this.props.modifyPermission(
                      permission,
                      namespace,
                      name,
                      this.permissionsModified
                    );
                  }}
                />
              );
            else return <PermissionRow key={index} permission={permission} />;
          })}
        </tbody>
      </table>
    );
  }
}

const mapStateToProps = (state, ownProps) => {};

const mapDispatchToProps = dispatch => {
  return {
    modifyPermission: (
      permission: Permission,
      namespace: string,
      name: string,
      callback: () => void
    ) => {
      dispatch(modifyPermission(permission, namespace, name, callback));
    }
  };
};
export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("permissions")(PermissionsTable));
