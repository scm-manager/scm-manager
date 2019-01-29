//@flow
import * as React from "react";
import classNames from "classnames";
import injectSheet from "react-jss";

type Props = {
  children?: React.Node,
  classes: Object
};

const styles = {
  wrapper: {
    padding: "1rem 1.5rem 0.25rem 1.5rem",
    border: "1px solid #eee",
    borderRadius: "5px 5px 0 0"
  }
};

class TableHeader extends React.Component<Props> {
  render() {
    const { classes, children } = this.props;
    return (
      <div
        className={classNames(
          "has-background-light field",
          "is-horizontal",
          classes.wrapper
        )}
      >
        {children}
      </div>
    );
  }
}

export default injectSheet(styles)(TableHeader);
