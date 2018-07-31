// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { Group } from "../../types/Group";

type Props = {
  group: Group
};

export default class GroupRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { group } = this.props;
    const to = `/group/${group.name}`;
    return (
      <tr>
        <td className="is-hidden-mobile">{this.renderLink(to, group.name)}</td>
        <td>{this.renderLink(to, group.description)}</td>
      </tr>
    );
  }
}
