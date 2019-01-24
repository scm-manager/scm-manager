// @flow
import React from "react";
import { translate } from "react-i18next";
import { Select } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  availableTypes: string[],
  handleTypeChange: string => void,
  type: string,
  label?: string,
  helpText?: string,
  loading?: boolean
};

class TypeSelector extends React.Component<Props> {
  render() {
    const {
      availableTypes,
      type,
      handleTypeChange,
      loading,
      label,
      helpText
    } = this.props;

    if (!availableTypes) return null;

    return (
      <Select
        onChange={handleTypeChange}
        value={type ? type : availableTypes[0]}
        options={this.createSelectOptions(availableTypes)}
        loading={loading}
        label={label}
        helpText={helpText}
      />
    );
  }

  createSelectOptions(types: string[]) {
    return types.map(type => {
      return {
        label: type,
        value: type
      };
    });
  }
}

export default translate("repos")(TypeSelector);
