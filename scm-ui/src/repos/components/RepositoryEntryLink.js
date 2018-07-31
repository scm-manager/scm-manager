//@flow
import React from "react";
import { Link } from "react-router-dom";
import injectSheet from "react-jss";
import classNames from "classnames";

const styles = {
  link: {
    pointerEvents: "all"
  }
};

type Props = {
  to: string,
  iconClass: string,

  // context props
  classes: any
};

class RepositoryEntryLink extends React.Component<Props> {
  render() {
    const { to, iconClass, classes } = this.props;
    return (
      <Link className={classNames("level-item", classes.link)} to={to}>
        <span className="icon is-small">
          <i className={classNames("fa", iconClass)} />
        </span>
      </Link>
    );
  }
}

export default injectSheet(styles)(RepositoryEntryLink);
