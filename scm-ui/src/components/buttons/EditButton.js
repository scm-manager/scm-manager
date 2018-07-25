//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";

class EditButton extends React.Component<ButtonProps> {
  render() {
    return <Button type="default" {...this.props} />;
  }
}

export default EditButton;
