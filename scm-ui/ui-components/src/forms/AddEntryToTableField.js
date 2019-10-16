//@flow
import React from "react";

import { AddButton } from "../buttons";
import InputField from "./InputField";

type Props = {
  addEntry: string => void,
  disabled: boolean,
  buttonLabel: string,
  fieldLabel: string,
  errorMessage: string,
  helpText?: string,
  validateEntry?: string => boolean
};

type State = {
  entryToAdd: string
};

class AddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      entryToAdd: ""
    };
  }

  isValid = () => {
    const {validateEntry} = this.props;
    if (!this.state.entryToAdd || this.state.entryToAdd === "" || !validateEntry) {
      return true;
    } else {
      return validateEntry(this.state.entryToAdd);
    }
  };

  render() {
    const {
      disabled,
      buttonLabel,
      fieldLabel,
      errorMessage,
      helpText
    } = this.props;
    return (
      <div className="field">
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
        <AddButton
          label={buttonLabel}
          action={this.addButtonClicked}
          disabled={disabled || this.state.entryToAdd ==="" || !this.isValid()}
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
    this.props.addEntry(entryToAdd);
    this.setState({ ...this.state, entryToAdd: "" });
  };

  handleAddEntryChange = (entryname: string) => {
    this.setState({
      ...this.state,
      entryToAdd: entryname
    });
  };
}

export default AddEntryToTableField;
