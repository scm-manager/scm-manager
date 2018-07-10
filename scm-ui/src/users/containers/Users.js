// @flow
import React from "react";
import { connect } from "react-redux";

import { fetchUsersIfNeeded, fetchUsers } from "../modules/users";
import Login from "../../containers/Login";
import UserRow from "./UserRow";

type Props = {
  login: boolean,
  error: any,
  users: any,
  fetchUsersIfNeeded: () => void,
  fetchUsers: () => void,
  fetchUsersIfNeeded: (url: string) => void,

};

class Users extends React.Component<Props> {
  componentWillMount() {
    this.props.fetchUsersIfNeeded();
  }

  componentDidUpdate() {
    this.props.fetchUsersIfNeeded();
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
                <th>E-Mail</th>
                <th>Admin</th>
              </tr>
            </thead>
            <tbody>
              {this.props.users.map((user, index) => {
                return <UserRow key={index} user={user} />;
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
    fetchUsersIfNeeded: () => {
      dispatch(fetchUsersIfNeeded());
    },
    fetchUsers: () => {
      dispatch(fetchUsers());
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Users);
