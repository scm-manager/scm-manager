// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { Branch } from "@scm-manager/ui-types";

type Props = {
  baseUrl: string,
  branch: Branch
};

export default class BranchRow extends React.Component<Props> {
  renderLink(to: string, label: string, defaultBranch: boolean) {
    let showLabel = null;
    if (defaultBranch) {
      showLabel = <span className="tag is-dark">Default</span>;
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
