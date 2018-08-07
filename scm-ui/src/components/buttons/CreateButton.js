//@flow
import React from "react";
import injectSheet from "react-jss";
import Button, { type ButtonProps } from "./Button";
import classNames from "classnames";

const styles = {
  spacing: {
    margin: "1em 0 0 1em"
  }
};

class CreateButton extends React.Component<ButtonProps> {
  render() {
    const { classes } = this.props;
    return (
      <div className={classNames("is-pulled-right", classes.spacing)}>
        <Button type="default" {...this.props} />
      </div>
    );
  }
}

export default injectSheet(styles)(CreateButton);
