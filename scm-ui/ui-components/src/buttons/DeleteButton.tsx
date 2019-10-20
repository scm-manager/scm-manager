import React from "react";
import Button, { ButtonProps } from "./Button";

class DeleteButton extends React.Component<ButtonProps> {
  render() {
    return <Button color="warning" icon="times" {...this.props} />;
  }
}

export default DeleteButton;
