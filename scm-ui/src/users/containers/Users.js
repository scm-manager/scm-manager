// @flow
import React from "react";
import { connect } from "react-redux";

import { fetchUsers, deleteUser, getUsersFromState } from "../modules/users";
import Loading from "../../components/Loading";
import ErrorNotification from "../../components/ErrorNotification";
import UserTable from "./UserTable";
import type { User } from "../types/User";
import AddButton from "../../components/AddButton";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  login: boolean,
  error: Error,
  userEntries: Array<UserEntry>,
  fetchUsers: () => void,
  deleteUser: User => void,
  canAddUsers: boolean
};

class Users extends React.Component<Props, User> {
  componentDidMount() {
    this.props.fetchUsers();
  }

  render() {
    return (
      <section className="section">
        <div className="container">
          <h1 className="title">SCM</h1>
          <h2 className="subtitle">Users</h2>
          {this.renderContent()}
          {this.renderAddButton()}
        </div>
      </section>
    );
  }

  renderContent() {
    const { userEntries, deleteUser, error } = this.props;
    if (userEntries) {
      return (
        <div>
          <ErrorNotification error={error} />
          <UserTable entries={userEntries} deleteUser={deleteUser} />
        </div>
      );
    } else {
      return <Loading />;
    }
  }

  renderAddButton() {
    if (this.props.canAddUsers) {
      return (
        <div>
          <AddButton label="Add User" link="/users/add" />
        </div>
      );
    } else {
      return;
    }
  }
}

const mapStateToProps = state => {
  const userEntries = getUsersFromState(state);
  let error = null;
  let canAddUsers = false;
  if (state.users && state.users.users) {
    error = state.users.users.error;
    canAddUsers = state.users.users.userCreatePermission;
  }
  return {
    userEntries,
    error,
    canAddUsers
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUsers: () => {
      dispatch(fetchUsers());
    },
    deleteUser: (user: User) => {
      dispatch(deleteUser(user));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Users);
