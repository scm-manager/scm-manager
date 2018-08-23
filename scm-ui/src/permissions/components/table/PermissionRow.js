// @flow
import React from "react";
import type { Permission } from "../../types/Permissions";
import { Checkbox } from "../../../components/forms";

type Props = {
  permission: Permission
};

export default class PermissionRow extends React.Component<Props> {
  render() {
    const { permission } = this.props;
    return (
      <tr>
        <td>{permission.name}</td>
        <td className="is-hidden-mobile">{permission.type}</td>
        <td>
          <Checkbox checked={permission ? permission.groupPermission : false} />
        </td>
      </tr>
    );
  }
}
