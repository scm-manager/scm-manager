//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";

class SubmitButton extends React.Component<ButtonProps> {
  render() {
    return <Button type="submit" color="primary" {...this.props} />;
  }
}

export default SubmitButton;
