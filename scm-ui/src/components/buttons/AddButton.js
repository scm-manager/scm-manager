//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";

class AddButton extends React.Component<ButtonProps> {
  render() {
    return <Button type="default" {...this.props} />;
  }
}

export default AddButton;
