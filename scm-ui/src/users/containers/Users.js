// @flow
import React from "react";
import { connect } from "react-redux";

import { fetchUsers, addUser, editUser, deleteUser } from "../modules/users";
import UserForm from "./UserForm";
import UserTable from "./UserTable";
import type { User } from "../types/User";

type Props = {
  login: boolean,
  error: Error,
  users: Array<User>,
  fetchUsers: () => void,
  deleteUser: string => void,
  addUser: User => void,
  editUser: User => void
};

class Users extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUsers();
  }

  addUser = (user: User) => {
    this.props.addUser(user);
  };

  editUser = (user: User) => {
    this.props.editUser(user);
  };

  render() {
    const { users, deleteUser } = this.props;
    const testUser: User = {
      name: "user",
      displayName: "user_display",
      password: "pw",
      mail: "mail@mail.de",
      active: true,
      admin: true
    };
    if (users) {
      return (
        <section className="section">
          <div className="container">
            <h1 className="title">SCM</h1>
            <h2 className="subtitle">Users</h2>
            <UserTable users={users} deleteUser={deleteUser} />
            {/* <UserForm submitForm={this.submitForm} /> */}
            <UserForm submitForm={user => {}} user={testUser} />
          </div>
        </section>
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
    addUser: (user: User) => {
      dispatch(addUser(user));
    },
    editUser: (user: User) => {
      dispatch(editUser(user));
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
