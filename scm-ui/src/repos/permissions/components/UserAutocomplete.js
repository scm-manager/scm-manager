// @flow
import React from "react";
import { translate } from "react-i18next";
import { Autocomplete } from "@scm-manager/ui-components";
import type { SelectValue } from "@scm-manager/ui-types";

type Props = {
  userAutocompleteLink: string,
  valueSelected: SelectValue => void,
  value: string,

  // Context props
  t: string => string
};

class UserAutocomplete extends React.Component<Props> {
  loadUserSuggestions = (inputValue: string) => {
    const url = this.props.userAutocompleteLink;
    const link = url + "?q=";
    return fetch(link + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          const label = element.displayName
            ? `${element.displayName} (${element.id})`
            : element.id;
          return {
            value: element,
            label
          };
        });
      });
  };

  selectName = (selection: SelectValue) => {
    this.props.valueSelected(selection);
  };

  render() {
    const { t, value } = this.props;
    return (
      <Autocomplete
        loadSuggestions={this.loadUserSuggestions}
        label={t("permission.user")}
        noOptionsMessage={t("permission.autocomplete.no-user-options")}
        loadingMessage={t("permission.autocomplete.loading")}
        placeholder={t("permission.autocomplete.user-placeholder")}
        valueSelected={this.selectName}
        value={value}
        creatable={true}
      />
    );
  }
}

export default translate("repos")(UserAutocomplete);
