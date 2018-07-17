// @flow
import React from "react";
import { connect } from "react-redux";

import {
  fetchUsers,
  addUser,
  updateUser,
  editUser,
  deleteUser,
  getUsersFromState
} from "../modules/users";
import UserForm from "./UserForm";
import UserTable from "./UserTable";
import type { User } from "../types/User";

type UserEntry = {
  loading: boolean,
  error: Error,
  user: User
};

type Props = {
  login: boolean,
  error: Error,
  userEntries: Array<UserEntry>,
  fetchUsers: () => void,
  deleteUser: string => void,
  addUser: User => void,
  updateUser: User => void,
  editUser: User => void,
  userToEdit: User
};

class Users extends React.Component<Props, User> {
  componentDidMount() {
    this.props.fetchUsers();
  }

  addUser = (user: User) => {
    this.props.addUser(user);
  };

  updateUser = (user: User) => {
    this.props.updateUser(user);
  };

  componentDidUpdate(prevProps: Props) {
    if (prevProps.userToEdit !== this.props.userToEdit) {
      this.setState(this.props.userToEdit);
    }
  }

  submitUser = (user: User) => {
    if (user._links && user._links.update) {
      this.updateUser(user);
    } else {
      this.addUser(user);
    }
  };

  render() {
    const { userEntries, deleteUser, editUser } = this.props;
    if (userEntries) {
      return (
        <section className="section">
          <div className="container">
            <h1 className="title">SCM</h1>
            <h2 className="subtitle">Users</h2>
            <UserTable
              entries={userEntries}
              deleteUser={deleteUser}
              editUser={(user: User) => {
                this.props.editUser(user);
              }}
            />
            <UserForm
              submitForm={user => this.submitUser(user)}
              user={this.props.userToEdit}
            />
          </div>
        </section>
      );
    } else {
      return <div>Loading...</div>;
    }
  }
}

const mapStateToProps = state => {
  const userEntries = getUsersFromState(state);
  var userToEdit = state.users.editUser;
  if (!userEntries) {
    return { userToEdit };
  }
  return {
    userEntries,
    userToEdit
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
    updateUser: (user: User) => {
      dispatch(updateUser(user));
    },
    deleteUser: (link: string) => {
      dispatch(deleteUser(link));
    },
    editUser: (user: User) => {
      dispatch(editUser(user));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Users);
