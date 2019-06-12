// @flow
import React from "react";
import type { SelectValue } from "@scm-manager/ui-types";
import Autocomplete from "./Autocomplete";

type Props = {
  autocompleteLink: string,
  valueSelected: SelectValue => void
};

class UserGroupAutocomplete extends React.Component<Props> {
  loadSuggestions = (inputValue: string) => {
    const url = this.props.autocompleteLink;
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
    return (
      <Autocomplete
        loadSuggestions={this.loadSuggestions}
        valueSelected={this.selectName}
        creatable={true}
        {...this.props}
      />
    );
  }
}

export default UserGroupAutocomplete;
