//@flow
import React from "react";
import injectSheet from "react-jss";
import { type ButtonProps } from "./Button";
import SubmitButton from "./SubmitButton";
import classNames from "classnames";

const styles = {
  spacing: {
    marginTop: "2em",
    border: "2px solid #e9f7fd",
    padding: "1em 1em"
  }
  
};

class CreateButton extends React.Component<ButtonProps> {
  render() {
    const { classes } = this.props;
    return (
      <div className={classNames("has-text-centered", classes.spacing)}>
        <SubmitButton {...this.props} />
      </div>
    );
  }
}

export default injectSheet(styles)(CreateButton);
