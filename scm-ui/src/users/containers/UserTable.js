// @flow
import React from "react";
import UserRow from "./UserRow";
import type { User } from "../types/User";

type Props = {
  users: Array<User>,
  deleteUser: string => void
};

class UserTable extends React.Component<Props> {
  render() {
    const { users, deleteUser } = this.props;
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
          {users.map((user, index) => {
            return <UserRow key={index} user={user} deleteUser={deleteUser} />;
          })}
        </tbody>
      </table>
    );
  }
}

export default UserTable;
