//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";

const styles = {
  tag: {
    marginTop: ".5rem"
  },
  spacing: {
    marginRight: ".25rem"
  }
};

type Props = {
  icon: string,
  label: string,

  // context props
  classes: Object
};

class ChangesetTagBase extends React.Component<Props> {
  render() {
    const { icon, label, classes } = this.props;
    return (
      <span className={classNames(classes.tag, "tag", "is-info")}>
        <span className={classNames("fa", icon, classes.spacing)} /> {label}
      </span>
    );
  }
}

export default injectSheet(styles)(ChangesetTagBase);
