// @flow
import React from "react";
import { translate } from "react-i18next";
import type { SelectValue } from "@scm-manager/ui-types";
import UserGroupAutocomplete from "./UserGroupAutocomplete";

type Props = {
  groupAutocompleteLink: string,
  valueSelected: SelectValue => void,
  value: string,

  // Context props
  t: string => string
};

class GroupAutocomplete extends React.Component<Props> {
  selectName = (selection: SelectValue) => {
    this.props.valueSelected(selection);
  };

  render() {
    const { groupAutocompleteLink, t, value } = this.props;
    return (
      <UserGroupAutocomplete
        autocompleteLink={groupAutocompleteLink}
        label={t("autocomplete.group")}
        noOptionsMessage={t("autocomplete.noGroupOptions")}
        loadingMessage={t("autocomplete.loading")}
        placeholder={t("autocomplete.groupPlaceholder")}
        valueSelected={this.selectName}
        value={value}
      />
    );
  }
}

export default translate("commons")(GroupAutocomplete);
