import React from "react";
import { Link } from "react-router-dom";
import { Branch } from "@scm-manager/ui-types";
import DefaultBranchTag from "./DefaultBranchTag";

type Props = {
  baseUrl: string;
  branch: Branch;
};

class BranchRow extends React.Component<Props> {
  renderLink(to: string, label: string, defaultBranch?: boolean) {
    return (
      <Link to={to}>
        {label} <DefaultBranchTag defaultBranch={defaultBranch} />
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

export default BranchRow;
