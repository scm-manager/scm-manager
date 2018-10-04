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
  helpText?: string
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
          validationError={false}
          value={this.state.entryToAdd}
          onReturnPressed={this.appendEntry}
          disabled={disabled}
          helpText={helpText}
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
