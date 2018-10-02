//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";

const styles = {
  img: {
    display: "block"
  },
  q: {
    float: "left",
    paddingLeft: "3px"
  }
};

type Props = {
  message?: string,
  classes: any
};

class Help extends React.Component<Props> {
  render() {
    const { message, classes } = this.props;
    return (
        <div className={classNames("tooltip is-tooltip-right", classes.q)}
             data-tooltip={message}>
        <i className={classNames("fa fa-question has-text-info", classes.img)}></i>
        </div>
    );
  }
}

export default injectSheet(styles)(Help);
