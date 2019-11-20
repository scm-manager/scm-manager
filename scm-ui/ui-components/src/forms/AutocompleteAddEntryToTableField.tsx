import React, { MouseEvent } from "react";
import { SelectValue } from "@scm-manager/ui-types";
import Autocomplete from "../Autocomplete";
import Level from "../layout/Level";
import AddButton from "../buttons/AddButton";

type Props = {
  addEntry: (p: SelectValue) => void;
  disabled: boolean;
  buttonLabel: string;
  fieldLabel: string;
  helpText?: string;
  loadSuggestions: (p: string) => Promise<SelectValue[]>;
  placeholder?: string;
  loadingMessage?: string;
  noOptionsMessage?: string;
};

type State = {
  selectedValue?: SelectValue;
};

class AutocompleteAddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      selectedValue: undefined
    };
  }
  render() {
    const {
      disabled,
      buttonLabel,
      fieldLabel,
      helpText,
      loadSuggestions,
      placeholder,
      loadingMessage,
      noOptionsMessage
    } = this.props;

    const { selectedValue } = this.state;
    return (
      <div className="field">
        <Autocomplete
          label={fieldLabel}
          loadSuggestions={loadSuggestions}
          valueSelected={this.handleAddEntryChange}
          helpText={helpText}
          value={selectedValue}
          placeholder={placeholder}
          loadingMessage={loadingMessage}
          noOptionsMessage={noOptionsMessage}
          creatable={true}
        />
        <Level right={<AddButton label={buttonLabel} action={this.addButtonClicked} disabled={disabled} />} />
      </div>
    );
  }

  addButtonClicked = (event: MouseEvent) => {
    event.preventDefault();
    this.appendEntry();
  };

  appendEntry = () => {
    const { selectedValue } = this.state;
    if (!selectedValue) {
      return;
    }
    this.setState(
      // @ts-ignore null is needed to clear the selection; undefined does not work
      {
        ...this.state,
        selectedValue: null
      },
      () => this.props.addEntry(selectedValue)
    );
  };

  handleAddEntryChange = (selection: SelectValue) => {
    this.setState({
      ...this.state,
      selectedValue: selection
    });
  };
}

export default AutocompleteAddEntryToTableField;
