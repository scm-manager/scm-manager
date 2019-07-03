// @flow
import React from "react";
import { translate } from "react-i18next";
import type AutocompleteProps from "./UserGroupAutocomplete";
import UserGroupAutocomplete from "./UserGroupAutocomplete";

type Props = AutocompleteProps & {
  // Context props
  t: string => string
};

class GroupAutocomplete extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <UserGroupAutocomplete
        label={t("autocomplete.group")}
        noOptionsMessage={t("autocomplete.noGroupOptions")}
        loadingMessage={t("autocomplete.loading")}
        placeholder={t("autocomplete.groupPlaceholder")}
        {...this.props}
      />
    );
  }
}

export default translate("commons")(GroupAutocomplete);
