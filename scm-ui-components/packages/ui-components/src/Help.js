//@flow
import React from "react";
import injectSheet from "react-jss";
import Tooltip from './Tooltip';
import HelpIcon from './HelpIcon';

const styles = {
  tooltip: {
    display: "inline-block",
    paddingLeft: "3px",
    position: "absolute"
  }
};

type Props = {
  message: string,
  classes: any
}

class Help extends React.Component<Props> {

  render() {
    const { message, classes } = this.props;
    return (
      <Tooltip className={classes.tooltip} message={message}>
        <HelpIcon />
      </Tooltip>
    );
  }

}

export default injectSheet(styles)(Help);

