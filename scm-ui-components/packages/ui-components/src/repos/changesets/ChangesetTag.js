//@flow
import React from "react";
import type { Tag } from "@scm-manager/ui-types";
import injectSheet from "react-jss";
import classNames from "classnames";

const styles = {
  spacing: {
    marginRight: "4px"
  }
};

type Props = {
  tag: Tag,

  // context props
  classes: Object
};

class ChangesetTag extends React.Component<Props> {
  render() {
    const { tag, classes } = this.props;
    return (
      <span className="tag is-info">
        <span className={classNames("fa", "fa-tag", classes.spacing)} />{" "}
        {tag.name}
      </span>
    );
  }
}

export default injectSheet(styles)(ChangesetTag);
