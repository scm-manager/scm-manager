// @flow
import React from "react";

type Props = {
  user: any
};

export default class UserRow extends React.Component<Props> {
  render() {
    return (
      <tr>
        <td>{this.props.user.displayName}</td>
        <td>{this.props.user.mail}</td>
        <td>
          <input type="checkbox" id="admin" checked={this.props.user.admin} />
        </td>
      </tr>
    );
  }
}
