//@flow
import React from 'react';

import InputField from "../../components/forms/InputField"
import SubmitButton from "../../components/buttons/SubmitButton"
import type { Group } from "../types/Group"
export interface Props {
  loading?: boolean,
  submitForm: Group => void
}

export interface State {
  group: Group
}

class GroupForm extends React.Component<Props, State> {

  isValid = () => {
    return true;
  }

  submit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state.group)
  }

  render() {
    const { loading } = this.props;
    return (
      // TODO: i18n
      <form onSubmit={this.submit}>
      <InputField label="Name" errorMessage="Invalid group name" onChange={()=>{}} validationError={false}/>
      <InputField label="Description" errorMessage="Invalid group description" onChange={()=>{}} validationError={false} />
      <SubmitButton
          disabled={!this.isValid()}
          loading={loading}
          label="Submit"
        />
      </form>
    )
  }

}

export default GroupForm;