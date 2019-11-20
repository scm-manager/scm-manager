import React, { MouseEvent } from "react";
import InputField from "./InputField";
import Level from "../layout/Level";
import { AddButton } from "../buttons";

type Props = {
  addEntry: (p: string) => void;
  disabled: boolean;
  buttonLabel: string;
  fieldLabel: string;
  errorMessage: string;
  helpText?: string;
  validateEntry?: (p: string) => boolean;
};

type State = {
  entryToAdd: string;
};

class AddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      entryToAdd: ""
    };
  }

  isValid = () => {
    const { validateEntry } = this.props;
    if (!this.state.entryToAdd || this.state.entryToAdd === "" || !validateEntry) {
      return true;
    } else {
      return validateEntry(this.state.entryToAdd);
    }
  };

  render() {
    const { disabled, buttonLabel, fieldLabel, errorMessage, helpText } = this.props;
    return (
      <>
        <InputField
          label={fieldLabel}
          errorMessage={errorMessage}
          onChange={this.handleAddEntryChange}
          validationError={!this.isValid()}
          value={this.state.entryToAdd}
          onReturnPressed={this.appendEntry}
          disabled={disabled}
          helpText={helpText}
        />
        <Level
          right={
            <AddButton
              label={buttonLabel}
              action={this.addButtonClicked}
              disabled={disabled || this.state.entryToAdd === "" || !this.isValid()}
            />
          }
        />
      </>
    );
  }

  addButtonClicked = (event: MouseEvent) => {
    event.preventDefault();
    this.appendEntry();
  };

  appendEntry = () => {
    const { entryToAdd } = this.state;
    this.props.addEntry(entryToAdd);
    this.setState({
      ...this.state,
      entryToAdd: ""
    });
  };

  handleAddEntryChange = (entryname: string) => {
    this.setState({
      ...this.state,
      entryToAdd: entryname
    });
  };
}

export default AddEntryToTableField;
