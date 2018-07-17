// @flow
import React from "react";
import DeleteUserButton from "./DeleteUserButton";
import EditUserButton from "./EditUserButton";
import type { User } from "../types/User";

type Props = {
  entry: { loading: boolean, error: Error, user: User },
  deleteUser: User => void,
  editUser: User => void
};

export default class UserRow extends React.Component<Props> {
  render() {
    const { deleteUser, editUser } = this.props;
    const user = this.props.entry.entry;
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
        <td>
          <EditUserButton user={user} editUser={editUser} />
        </td>
      </tr>
    );
  }
}          
