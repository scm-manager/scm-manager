// @flow
import React from "react";
import AsyncSelect from "react-select/lib/Async";

type SelectionResult = {
  id: string,
  displayName: string
};

type SelectValue = {
  value: SelectionResult,
  label: string
};

type Props = {
  url: string,
  loadOptions: string => Promise<SelectionResult>,
  valueSelected: SelectionResult => void
};

type State = {
  value: SelectionResult
};

const URL_QUERY_SUFFIX: string = "?q=";

class AsyncAutocomplete extends React.Component<Props, State> {
  getOptions = (inputValue: string) => {
    const { url } = this.props;
    return fetch(url + URL_QUERY_SUFFIX + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          return { value: element, label: element.displayName };
        });
      });
  };

  handleInputChange = (newValue: SelectValue) => {
    this.setState({ value: newValue.value });
    return newValue.value;
  };

  render() {
    return (
      <AsyncSelect
        cacheOptions
        defaultOptions
        loadOptions={this.getOptions}
        onChange={this.handleInputChange}
      />
    );
  }
}

export default AsyncAutocomplete;
