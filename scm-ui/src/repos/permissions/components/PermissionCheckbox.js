// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  disabled: boolean,
  name: string,
  checked: boolean,
  onChange?: (value: boolean, name?: string) => void
};

class PermissionCheckbox extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <Checkbox
        key={this.props.name}
        name={this.props.name}
        helpText={t("verbs.repository." + this.props.name + ".description")}
        label={t("verbs.repository." + this.props.name + ".displayName")}
        checked={this.props.checked}
        onChange={this.props.onChange}
        disabled={this.props.disabled}
      />
    );
  }
}

export default translate("plugins")(PermissionCheckbox);
