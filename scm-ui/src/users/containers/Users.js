// @flow
import React from "react";
import { connect } from "react-redux";

import { fetchUsers, deleteUser } from "../modules/users";
import Login from "../../containers/Login";
import UserRow from "./UserRow";
import type { User } from "../types/User";

type Props = {
  login: boolean,
  error: Error,
  users: Array<User>,
  fetchUsers: () => void,
  deleteUser: string => void
};

class Users extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUsers();
  }

  render() {
    if (this.props.users) {
      return (
        <div>
          <h1>SCM</h1>
          <h2>Users</h2>
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
              {this.props.users.map((user, index) => {
                return (
                  <UserRow
                    key={index}
                    user={user}
                    deleteUser={this.props.deleteUser}
                  />
                );
              })}
            </tbody>
          </table>
        </div>
      );
    } else {
      return <div>Loading...</div>;
    }
  }
}

const mapStateToProps = state => {
  return {
    users: state.users.users
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUsers: () => {
      dispatch(fetchUsers());
    },
    deleteUser: (link: string) => {
      dispatch(deleteUser(link));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Users);
