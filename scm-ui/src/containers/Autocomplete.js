// @flow
import React from "react";
import Autosuggest from "react-autosuggest";
import { apiClient } from "@scm-manager/ui-components";

const getSuggestionValue = suggestion => suggestion.displayName;
const renderSuggestion = suggestion => <div>{suggestion.displayName}</div>;

type Props = {
  url: string,
  placeholder: string,
  timeoutMillis: number
};

type State = {
  suggestions: any[],
  value: any,
  isLoading: boolean,
  lastRequestId: any
};

class Autocomplete extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      suggestions: [],
      value: "",
      isLoading: false,
      lastRequestId: undefined
    };
  }

  loadSuggestions = (value: string) => {
    this.setState({
      isLoading: true
    });

    if (this.state.lastRequestId) {
      clearTimeout(this.state.lastRequestId);
    }

    const requestId = setTimeout(() => {
      this.setState({
        isLoading: true
      });

      apiClient
        .get(this.props.url + value)
        .then(response => {
          return response.json();
        })
        .then(json => {
          this.setState({
            isLoading: false,
            suggestions: [...json]
          });
        });
    }, this.props.timeoutMillis);

    this.setState({
      lastRequestId: requestId
    });
  };

  // TODO: Flow types
  onChange = (event: SyntheticInputEvent<HTMLInputElement>, { newValue }) => {
    this.setState({
      value: newValue
    });
  };

  // TODO: Flow types
  onSuggestionsFetchRequested = ({ value }) => {
    this.loadSuggestions(value);
  };

  onSuggestionsClearRequested = () => {
    this.setState({
      suggestions: []
    });
  };

  render() {
    const { value, suggestions } = this.state;

    const inputProps = {
      placeholder: this.props.placeholder,
      value,
      onChange: this.onChange
    };

    return (
      <Autosuggest
        suggestions={suggestions}
        onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
        onSuggestionsClearRequested={this.onSuggestionsClearRequested}
        getSuggestionValue={getSuggestionValue}
        renderSuggestion={renderSuggestion}
        inputProps={inputProps}
      />
    );
  }
}

export default Autocomplete;
