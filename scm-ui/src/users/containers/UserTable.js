// @flow
import React from "react";
import UserRow from "./UserRow";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  entries: Array<UserEntry>,
  deleteUser: string => void,
  editUser: User => void
};

class UserTable extends React.Component<Props> {
  render() {
    const { deleteUser, editUser } = this.props;
    const entries = this.props.entries;
    return (
      <table className="table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>Name</th>
            <th>Display Name</th>
            <th>E-Mail</th>
            <th>Admin</th>
            <th />
            <th />
          </tr>
        </thead>
        <tbody>
          {entries.map((entry, index) => {
            return (
              <UserRow
                key={index}
                entry={entry}
                deleteUser={deleteUser}
                editUser={editUser}
              />
            );
          })}
        </tbody>
      </table>
    );
  }
}

export default UserTable;
