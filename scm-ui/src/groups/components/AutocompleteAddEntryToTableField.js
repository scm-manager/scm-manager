//@flow
import React from "react";

import { AddButton } from "@scm-manager/ui-components";
import Autocomplete from "../../containers/Autocomplete";
import type { AutocompleteObject } from "../../containers/Autocomplete";

type Props = {
  addEntry: string => void,
  disabled: boolean,
  buttonLabel: string,
  fieldLabel: string,
  helpText?: string,
  loadSuggestions: string => Promise<AutocompleteObject>
};

type State = {
  entryToAdd?: AutocompleteObject
};

class AutocompleteAddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { entryToAdd: undefined };
  }
  render() {
    const { disabled, buttonLabel, fieldLabel, helpText } = this.props;

    const { entryToAdd } = this.state;
    return (
      <div className="field">
        <Autocomplete
          label={fieldLabel}
          loadSuggestions={this.props.loadSuggestions}
          valueSelected={this.handleAddEntryChange}
          helpText={helpText}
          value={entryToAdd}
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
    if (!entryToAdd) {
      return;
    }
    this.setState({ ...this.state, entryToAdd: undefined }, () =>
      this.props.addEntry(entryToAdd.id)
    );
  };

  handleAddEntryChange = (selection: AutocompleteObject) => {
    this.setState({
      ...this.state,
      entryToAdd: selection
    });
  };
}

export default AutocompleteAddEntryToTableField;
