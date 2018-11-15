//@flow
import React from "react";

import { AddButton } from "@scm-manager/ui-components";
import AsyncAutocomplete from "../../containers/AsyncAutocomplete";

type Props = {
  addEntry: string => void,
  disabled: boolean,
  buttonLabel: string,
  fieldLabel: string,
  helpText?: string,
  loadSuggestions: string => any //TODO: type
};

type State = {
  entryToAdd: any // TODO: type
};

class AutocompleteAddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      entryToAdd: undefined
    };
  }

  render() {
    const { disabled, buttonLabel, fieldLabel, helpText } = this.props;

    const { entryToAdd } = this.state;

    return (
      <div className="field">
        <AsyncAutocomplete
          label={fieldLabel}
          loadSuggestions={this.props.loadSuggestions}
          valueSelected={this.handleAddEntryChange}
          helpText={helpText}
          value={entryToAdd ? entryToAdd.id : ""}
        />

        <AddButton
          label={buttonLabel}
          action={this.addButtonClicked}
          disabled={disabled}
        />
      </div>
    );
  }

  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.appendEntry();
  };

  appendEntry = () => {
    const { entryToAdd } = this.state;
    this.props.addEntry(entryToAdd.id);
    this.setState({ ...this.state, entryToAdd: undefined });
  };

  handleAddEntryChange = (selection: any) => {
    this.setState({
      ...this.state,
      entryToAdd: selection
    });
  };
}

export default AutocompleteAddEntryToTableField;
