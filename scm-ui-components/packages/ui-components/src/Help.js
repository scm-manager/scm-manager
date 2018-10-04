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
    paddingLeft: "3px",
    float: "right"
  }
};

type Props = {
  message: string,
  classes: any
};

class Help extends React.Component<Props> {
  render() {
    const { message, classes } = this.props;
    const multiline = message.length > 60 ? "is-tooltip-multiline" : "";
    return (
      <div
        className={classNames("tooltip is-tooltip-right", multiline, classes.q)}
        data-tooltip={message}
      >
        <i
          className={classNames("fa fa-question has-text-info", classes.img)}
        />
      </div>
    );
  }
}

export default injectSheet(styles)(Help);
