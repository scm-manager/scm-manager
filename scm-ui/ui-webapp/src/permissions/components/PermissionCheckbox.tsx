import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Checkbox } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  name: string;
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled: boolean;
  role?: boolean;
};

class PermissionCheckbox extends React.Component<Props> {
  render() {
    const { name, checked, onChange, disabled, role, t } = this.props;

    const key = name.split(":").join(".");
    const label = role
      ? t("verbs.repository." + name + ".displayName")
      : this.translateOrDefault("permissions." + key + ".displayName", key);
    const helpText = role
      ? t("verbs.repository." + name + ".description")
      : this.translateOrDefault("permissions." + key + ".description", t("permissions.unknown"));

    return (
      <Checkbox
        key={name}
        name={name}
        label={label}
        helpText={helpText}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
      />
    );
  }

  translateOrDefault = (key: string, defaultText: string) => {
    const translation = this.props.t(key);
    if (translation === key) {
      return defaultText;
    } else {
      return translation;
    }
  };
}

export default withTranslation("plugins")(PermissionCheckbox);
