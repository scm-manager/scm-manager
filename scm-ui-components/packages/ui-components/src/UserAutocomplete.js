// @flow
import React from "react";
import { translate } from "react-i18next";
import type { SelectValue } from "@scm-manager/ui-types";
import UserGroupAutocomplete from "./UserGroupAutocomplete";

type Props = {
  userAutocompleteLink: string,
  valueSelected: SelectValue => void,
  value?: SelectValue,

  // Context props
  t: string => string
};

class UserAutocomplete extends React.Component<Props> {
  selectName = (selection: SelectValue) => {
    this.props.valueSelected(selection);
  };

  render() {
    const { userAutocompleteLink, t, value } = this.props;
    return (
      <UserGroupAutocomplete
        autocompleteLink={userAutocompleteLink}
        label={t("autocomplete.user")}
        noOptionsMessage={t("autocomplete.noUserOptions")}
        loadingMessage={t("autocomplete.loading")}
        placeholder={t("autocomplete.userPlaceholder")}
        valueSelected={this.selectName}
        value={value}
      />
    );
  }
}

export default translate("commons")(UserAutocomplete);
