// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";

import { fetchUsers, getUsersFromState } from "../modules/users";
import Page from "../../components/Page";
import UserTable from "./UserTable";
import type { User } from "../types/User";
import AddButton from "../../components/AddButton";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  loading?: boolean,
  error: Error,
  t: string => string,
  userEntries: Array<UserEntry>,
  fetchUsers: () => void,
  canAddUsers: boolean
};

class Users extends React.Component<Props, User> {
  componentDidMount() {
    this.props.fetchUsers();
  }

  render() {
    const { userEntries, loading, t, error } = this.props;
    return (
      <Page
        title={t("users.title")}
        subtitle={t("users.subtitle")}
        loading={loading || !userEntries}
        error={error}
      >
        <UserTable entries={userEntries} />
        {this.renderAddButton()}
      </Page>
    );
  }

  renderAddButton() {
    const { canAddUsers, t } = this.props;
    if (canAddUsers) {
      return (
        <div>
          <AddButton label={t("users.add-button")} link="/users/add" />
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
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(Users));
