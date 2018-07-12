// @flow
import React from "react";
import UserRow from "./UserRow";
import type { User } from "../types/User";

type Props = {
  entries: [{ loading: boolean, error: Error, user: User }],
  deleteUser: string => void
};

class UserTable extends React.Component<Props> {
  render() {
    const { deleteUser } = this.props;
    const entries = this.props.entries;
    return (
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Display Name</th>
            <th>E-Mail</th>
            <th>Admin</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry, index) => {
            return (
              <UserRow key={index} entry={entry} deleteUser={deleteUser} />
            );
          })}
        </tbody>
      </table>
    );
  }
}

export default UserTable;
