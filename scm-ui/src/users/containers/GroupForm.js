//@flow
import React from 'react';

import InputField from "../../components/forms/InputField"
export interface Props {
}

export interface State {
}

class GroupForm extends React.Component<Props, State> {

  render() {
    return (
      <form>
      <InputField label="Name" errorMessage="" onChange={()=>{}} validationError={false}/>
      </form>
    )
  }

}

export default GroupForm;