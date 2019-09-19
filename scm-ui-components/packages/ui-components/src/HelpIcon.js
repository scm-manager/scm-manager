//@flow
import React from "react";
import injectSheet from "react-jss";
import Icon from "./Icon";

type Props = {
  classes: any
};

const styles = {
  textinfo: {
    color: "#98d8f3 !important"
  }
};

class HelpIcon extends React.Component<Props> {
  render() {
    const { classes } = this.props;
    return (
      <Icon className={classes.textinfo} name="question-circle" />
    );
  }
}

export default injectSheet(styles)(HelpIcon);
