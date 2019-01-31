// @flow
import React from "react";
import { translate } from "react-i18next";
import { Select } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  availableRoles: string[],
  handleRoleChange: string => void,
  role: string,
  label?: string,
  helpText?: string,
  loading?: boolean
};

class RoleSelector extends React.Component<Props> {
  render() {
    const {
      availableRoles,
      role,
      handleRoleChange,
      loading,
      label,
      helpText
    } = this.props;

    if (!availableRoles) return null;

    const options = role
      ? this.createSelectOptions(availableRoles)
      : ["", ...this.createSelectOptions(availableRoles)];

    return (
      <Select
        onChange={handleRoleChange}
        value={role ? role : ""}
        options={options}
        loading={loading}
        label={label}
        helpText={helpText}
      />
    );
  }

  createSelectOptions(roles: string[]) {
    return roles.map(role => {
      return {
        label: role,
        value: role
      };
    });
  }
}

export default translate("repos")(RoleSelector);
