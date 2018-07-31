// @flow
import React from "react";
import { Link } from "react-router-dom";

type Props = {
  member: string
};

export default class GroupMember extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { member } = this.props;
    const to = `/user/${member}`;
    return (
      <tr className="is-hidden-mobile">
        <td>
          {this.renderLink(to, member)}
        </td>
      </tr>

    );
  }
}
