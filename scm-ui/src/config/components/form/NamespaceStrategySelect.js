//@flow
import React from "react";
import { translate, type TFunction } from "react-i18next";
import { Select } from "@scm-manager/ui-components";
import type { NamespaceStrategies } from "@scm-manager/ui-types";

type Props = {
  namespaceStrategies: NamespaceStrategies,
  label: string,
  value?: string,
  disabled?: boolean,
  helpText?: string,
  onChange: (value: string, name?: string) => void,
  // context props
  t: TFunction
};

class NamespaceStrategySelect extends React.Component<Props> {
  createNamespaceOptions = () => {
    const { namespaceStrategies, t } = this.props;
    let available = [];
    if (namespaceStrategies && namespaceStrategies.available) {
      available = namespaceStrategies.available;
    }

    return available.map(ns => {
      const key = "namespaceStrategies." + ns;
      let label = t("namespaceStrategies." + ns);
      if (label === key) {
        label = ns;
      }
      return {
        value: ns,
        label: label
      };
    });
  };

  render() {
    const { label, value, helpText, disabled, onChange } = this.props;
    const nsOptions = this.createNamespaceOptions();
    return (
      <Select
        label={label}
        onChange={onChange}
        value={value}
        disabled={disabled}
        options={nsOptions}
        helpText={helpText}
      />
    );
  }
}

export default translate("plugins")(NamespaceStrategySelect);
