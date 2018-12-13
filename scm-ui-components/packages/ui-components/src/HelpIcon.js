//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";

type Props = {
};

const styles = {
    textinfo: {
      color: "#98d8f3 !important" 
    }
};

class HelpIcon extends React.Component<Props> {
  render() {
    const { classes } = this.props;
    return <i className={classNames("fa fa-question-circle has-text-info", classes.textinfo)}></i>
  }
}

export default injectSheet(styles)(HelpIcon);
