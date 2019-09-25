//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";

class AddButton extends React.Component<ButtonProps> {
  render() {
    return <Button color="default" icon="plus" {...this.props} />;
  }
}

export default AddButton;
