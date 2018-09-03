//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";

class DeleteButton extends React.Component<ButtonProps> {
  render() {
    return <Button color="warning" {...this.props} />;
  }
}

export default DeleteButton;
