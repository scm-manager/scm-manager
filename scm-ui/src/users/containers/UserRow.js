// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { User } from "../types/User";

type Props = {
  user: User
};

export default class UserRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { user } = this.props;
    const to = `/user/${user.name}`;
    return (
      <tr>
        <td className="is-hidden-mobile">{this.renderLink(to, user.name)}</td>
        <td>{this.renderLink(to, user.displayName)}</td>
        <td>
          <a href={`mailto: ${user.mail}`}>{user.mail}</a>
        </td>
        <td className="is-hidden-mobile">
          <input type="checkbox" id="admin" checked={user.admin} readOnly />
        </td>
      </tr>
    );
  }
}
