// @flow
import * as React from "react";
import classNames from "classnames";
import injectSheet from "react-jss";

const styles = {
  container: {
    border: "2px solid #e9f7fd",
    padding: "1em 1em",
    marginTop: "2em",
    display: "flex",
    justifyContent: "center"
  }
};

type Props = {
  children?: React.Node,

  // context props
  classes: any
};

class PluginBottomActions extends React.Component<Props> {
  render() {
    const { children, classes } = this.props;
    return <div className={classNames(classes.container)}>{children}</div>;
  }
}

export default injectSheet(styles)(PluginBottomActions);
