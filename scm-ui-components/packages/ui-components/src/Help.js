//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";
import Tooltip from "./Tooltip";
import HelpIcon from "./HelpIcon";

const styles = {
  tooltip: {
    display: "inline-block",
    paddingLeft: "3px",
    position: "absolute"
  }
};

type Props = {
  message: string,
  className?: string,
  classes: any
};

class Help extends React.Component<Props> {
  render() {
    const { message, className, classes } = this.props;
    return (
      <Tooltip
        className={classNames(classes.tooltip, className)}
        message={message}
      >
        <HelpIcon />
      </Tooltip>
    );
  }
}

export default injectSheet(styles)(Help);
