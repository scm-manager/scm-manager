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
    borderBottom: "1px solid #dbdbdb"
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
