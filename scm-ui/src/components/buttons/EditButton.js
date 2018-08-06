//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";

class EditButton extends React.Component<ButtonProps> {
  render() {
    return <Button color="default" {...this.props} />;
  }
}

export default EditButton;
