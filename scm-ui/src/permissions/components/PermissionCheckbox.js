// @flow

import React from "react";
import { translate } from "react-i18next";
import { Checkbox } from "../../../../scm-ui-components/packages/ui-components/src";

type Props = {
  permission: string,
  checked: boolean,
  onChange: (value: boolean, name: string) => void,
  disabled: boolean,
  t: string => string
};

class PermissionCheckbox extends React.Component<Props> {
  render() {
    const { t, permission, checked, onChange, disabled } = this.props;
    const key = permission.split(":").join(".");
    console.log("permissions." + key + ".displayName");
    return (
      <Checkbox
        name={permission}
        label={t("permissions." + key + ".displayName")}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
        helpText={t("permissions." + key + ".description")}
      />
    );
  }
}

export default translate("plugins")(PermissionCheckbox);
