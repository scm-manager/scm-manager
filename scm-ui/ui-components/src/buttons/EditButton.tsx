import React from "react";
import Button, { ButtonProps } from "./Button";

class EditButton extends React.Component<ButtonProps> {
  render() {
    return <Button color="default" {...this.props} />;
  }
}

export default EditButton;
