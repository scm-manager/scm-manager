// @flow
import React from "react";
import { connect } from "react-redux";

import { fetchUsers, deleteUser, getUsersFromState } from "../modules/users";
import Page from "../../components/Page";
import ErrorNotification from "../../components/ErrorNotification";
import UserTable from "./UserTable";
import type { User } from "../types/User";
import AddButton from "../../components/AddButton";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  loading?: boolean,
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
    const { userEntries, deleteUser, loading, error } = this.props;
    return (
      <Page
        title="Users"
        subtitle="Create, read, update and delete users"
        loading={loading || !userEntries}
        error={error}
      >
        <UserTable entries={userEntries} deleteUser={deleteUser} />
        {this.renderAddButton()}
      </Page>
    );
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
  let loading = false;
  let canAddUsers = false;
  if (state.users && state.users.users) {
    error = state.users.users.error;
    canAddUsers = state.users.users.userCreatePermission;
    loading = state.users.users.loading;
  }
  return {
    userEntries,
    error,
    loading,
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
