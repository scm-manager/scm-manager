import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import AutocompleteProps from "./UserGroupAutocomplete";
import UserGroupAutocomplete from "./UserGroupAutocomplete";

class GroupAutocomplete extends React.Component<AutocompleteProps & WithTranslation> {
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

export default withTranslation("commons")(GroupAutocomplete);
