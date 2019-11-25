import React from "react";
import { SelectValue, AutocompleteObject } from "@scm-manager/ui-types";
import Autocomplete from "./Autocomplete";
import { apiClient } from "./apiclient";

export type AutocompleteProps = {
  autocompleteLink?: string;
  valueSelected?: (p: SelectValue) => void;
  value?: SelectValue;
};

type Props = AutocompleteProps & {
  label: string;
  noOptionsMessage: string;
  loadingMessage: string;
  placeholder: string;
};

export default class UserGroupAutocomplete extends React.Component<Props> {
  loadSuggestions = (inputValue: string): Promise<SelectValue[]> => {
    const url = this.props.autocompleteLink;
    const link = url + "?q=";
    return apiClient
      .get(link + inputValue)
      .then(response => response.json())
      .then((json: AutocompleteObject[]) => {
        return json.map(element => {
          const label = element.displayName ? `${element.displayName} (${element.id})` : element.id;
          return {
            value: element,
            label
          };
        });
      });
  };

  selectName = (selection: SelectValue) => {
    if (this.props.valueSelected) {
      this.props.valueSelected(selection);
    }
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
