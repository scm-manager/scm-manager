// @flow
import React from "react";
import Autosuggest from "react-autosuggest";
import { apiClient } from "@scm-manager/ui-components";

const getSuggestionValue = suggestion => suggestion.displayName;
const renderSuggestion = suggestion => <div>{suggestion.displayName}</div>;

type Props = {
  url: string
};

type State = {
  suggestions: any[],
  value: any,
  isLoading: boolean
};

class Autocomplete extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      suggestions: [],
      value: "",
      users: [],
      isLoading: false
    };
  }

  loadSuggestions = (value: string) => {
    this.setState({
      isLoading: true
    });

    apiClient
      .get("http://localhost:8081/scm/api/v2/autocomplete/users?q=" + value) //TODO: Do not hardcode URL
      .then(response => {
        return response.json();
      })
      .then(json => {
        this.setState({
          suggestions: [...this.state.suggestions, ...json]
        });
      });
  };

  onChange = (event, { newValue }) => {
    // TODO: Flow types
    this.setState({
      value: newValue
    });
  };

  onSuggestionsFetchRequested = ({ value }) => {
    // TODO: Flow types
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
      placeholder: "placeholder", // TODO: i18n
      value,
      onChange: this.onChange
    };

    // Finally, render it!
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
