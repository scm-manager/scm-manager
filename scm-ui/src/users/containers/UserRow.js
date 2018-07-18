// @flow
import React from "react";
import DeleteUserButton from "./DeleteUserButton";
import EditUserButton from "./EditUserButton";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  entry: UserEntry,
  deleteUser: User => void,
  editUser: User => void
};

export default class UserRow extends React.Component<Props> {
  render() {
    const { entry, deleteUser, editUser } = this.props;
    const user = entry.entry;
    return (
      <tr>
        <td>{user.name}</td>
        <td>{user.displayName}</td>
        <td>{user.mail}</td>
        <td>
          <input type="checkbox" id="admin" checked={user.admin} readOnly />
        </td>
        <td>
          <DeleteUserButton entry={entry} deleteUser={deleteUser} />
        </td>
        <td>
          <EditUserButton entry={entry} editUser={editUser} />
        </td>
      </tr>
    );
  }
}
