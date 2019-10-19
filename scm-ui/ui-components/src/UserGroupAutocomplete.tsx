import React from 'react';
import { SelectValue } from '@scm-manager/ui-types';
import Autocomplete from './Autocomplete';

export type AutocompleteProps = {
  autocompleteLink: string;
  valueSelected: (p: SelectValue) => void;
  value?: SelectValue;
};

type Props = AutocompleteProps & {
  label: string;
  noOptionsMessage: string;
  loadingMessage: string;
  placeholder: string;
};

export default class UserGroupAutocomplete extends React.Component<Props> {
  loadSuggestions = (inputValue: string) => {
    const url = this.props.autocompleteLink;
    const link = url + '?q=';
    return fetch(link + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          const label = element.displayName
            ? `${element.displayName} (${element.id})`
            : element.id;
          return {
            value: element,
            label,
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
