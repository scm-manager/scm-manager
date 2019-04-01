// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { Branch } from "@scm-manager/ui-types";
import injectSheet from "react-jss";
import classNames from "classnames";

type Props = {
  baseUrl: string,
  branch: Branch,
  classes: any
};

const styles = {
  tag: {
    marginLeft: "0.75rem",
    verticalAlign: "inherit"
  }
};

class BranchRow extends React.Component<Props> {
  renderLink(to: string, label: string, defaultBranch: boolean) {
    const { classes } = this.props;

    let showLabel = null;
    if (defaultBranch) {
      showLabel = <span className={classNames("tag is-dark", classes.tag)}>Default</span>;
    }
    return (
      <Link to={to}>
        {label} {showLabel}
      </Link>
    );
  }

  render() {
    const { baseUrl, branch } = this.props;
    const to = `${baseUrl}/${encodeURIComponent(branch.name)}/info`;
    return (
      <tr>
        <td>{this.renderLink(to, branch.name, branch.defaultBranch)}</td>
      </tr>
    );
  }
}

export default injectSheet(styles)(BranchRow);
