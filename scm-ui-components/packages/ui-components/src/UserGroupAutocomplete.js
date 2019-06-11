// @flow
import React from "react";
import type { SelectValue } from "@scm-manager/ui-types";
import Autocomplete from "./Autocomplete";

type Props = {
  autocompleteLink: string,
  valueSelected: SelectValue => void,
  value: string,
  label: string
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
    const { value, label } = this.props;
    return (
      <Autocomplete
        loadSuggestions={this.loadSuggestions}
        valueSelected={this.selectName}
        value={value}
        creatable={true}
        label={label}
        {...this.props}
      />
    );
  }
}

export default UserGroupAutocomplete;
