// @flow
import React from "react";
import DeleteUserButton from "./DeleteUserButton";
import type { User } from "../types/User";

type Props = {
  user: User,
  deleteUser: string => void
};

export default class UserRow extends React.Component<Props> {
  render() {
    const { user, deleteUser } = this.props;
    return (
      <tr>
        <td>{user.name}</td>
        <td>{user.displayName}</td>
        <td>{user.mail}</td>
        <td>
          <input type="checkbox" id="admin" checked={user.admin} readOnly />
        </td>
        <td>
          <DeleteUserButton user={user} deleteUser={deleteUser} />
        </td>
      </tr>
    );
  }
}
