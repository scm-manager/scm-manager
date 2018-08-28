// @flow
import React from "react";
import type { Permission } from "../types/Permissions";
import { translate } from "react-i18next";

type Props = {
  t: string => string
};

type State = {
  permission: Permission
};

class CreatePermissionForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      permission: {
        name: "",
        type: "READ",
        groupPermission: false,
        _links: {}
      }
    };
  }

  render() {
    return "Show Permissions here!";
  }
}

export default translate("permissions")(CreatePermissionForm);
