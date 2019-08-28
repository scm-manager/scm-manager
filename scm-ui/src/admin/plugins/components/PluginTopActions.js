// @flow
import * as React from "react";
import classNames from "classnames";
import injectSheet from "react-jss";

const styles = {
  container: {
    display: "flex",
    justifyContent: "flex-end",
    alignItems: "center"
  }
};

type Props = {
  children?: React.Node,

  // context props
  classes: any
};

class PluginTopActions extends React.Component<Props> {
  render() {
    const { children, classes } = this.props;
    return (
      <div className={classNames(classes.container, "column", "is-one-fifths", "is-mobile-action-spacing")}>
        {children}
      </div>
    );
  }
}

export default injectSheet(styles)(PluginTopActions);
