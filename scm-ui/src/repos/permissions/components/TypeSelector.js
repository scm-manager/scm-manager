// @flow
import React from "react";
import { translate } from "react-i18next";
import {
  Select
} from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  handleTypeChange: string => void,
  type: string,
  loading?: boolean
};

class TypeSelector extends React.Component<Props> {
  render() {
    const { type, handleTypeChange, loading } = this.props;
    const types = ["READ", "OWNER", "WRITE"];

    return (
      <Select
        onChange={handleTypeChange}
        value={type ? type : "READ"}
        options={this.createSelectOptions(types)}
        loading={loading}
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

export default translate("permissions")(TypeSelector);
