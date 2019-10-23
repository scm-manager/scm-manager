import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import AutocompleteProps from "./UserGroupAutocomplete";
import UserGroupAutocomplete from "./UserGroupAutocomplete";

class UserAutocomplete extends React.Component<AutocompleteProps & WithTranslation> {
  render() {
    const { t } = this.props;
    return (
      <UserGroupAutocomplete
        label={t("autocomplete.user")}
        noOptionsMessage={t("autocomplete.noUserOptions")}
        loadingMessage={t("autocomplete.loading")}
        placeholder={t("autocomplete.userPlaceholder")}
        {...this.props}
      />
    );
  }
}

export default withTranslation("commons")(UserAutocomplete);
