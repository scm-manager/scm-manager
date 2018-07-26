// @flow
import React from "react";
import type { History } from "history";
import { connect } from "react-redux";
import { translate } from "react-i18next";

import {
  fetchUsersByPage,
  fetchUsersByLink,
  getUsersFromState,
  selectList
} from "../modules/users";

import { Page } from "../../components/layout";
import { UserTable } from "./../components/table";
import type { User } from "../types/User";
import { AddButton } from "../../components/buttons";
import type { UserEntry } from "../types/UserEntry";
import type { PagedCollection } from "../../types/Collection";
import Paginator from "../../components/Paginator";
import CreateUserButton from "../components/buttons/CreateUserButton";

type Props = {
  loading?: boolean,
  error: Error,
  t: string => string,
  userEntries: Array<UserEntry>,
  fetchUsersByPage: (page: number) => void,
  fetchUsersByLink: (link: string) => void,
  canAddUsers: boolean,
  list?: PagedCollection,
  history: History,
  match: any,
  page: number
};

class Users extends React.Component<Props, User> {
  componentDidMount() {
    this.props.fetchUsersByPage(this.props.page);
  }

  onPageChange = (link: string) => {
    this.props.fetchUsersByLink(link);
  };

  componentDidUpdate = (prevProps: Props) => {
    const { page, list } = this.props;
    if (list) {
      // backend starts with 0
      const statePage: number = list.page + 1;
      if (page !== statePage) {
        this.props.history.push(`/users/${statePage}`);
      }
    }
  };

  render() {
    const { userEntries, list, loading, t, error } = this.props;
    return (
      <Page
        title={t("users.title")}
        subtitle={t("users.subtitle")}
        loading={loading || !userEntries}
        error={error}
      >
        <UserTable entries={userEntries} />
        {this.renderPaginator()}
        {this.renderAddButton()}
      </Page>
    );
  }

  renderPaginator() {
    const { list } = this.props;
    if (list) {
      return <Paginator collection={list} onPageChange={this.onPageChange} />;
    }
    return null;
  }

  renderAddButton() {
    if (this.props.canAddUsers) {
      return <CreateUserButton />;
    } else {
      return;
    }
  }
}

const mapStateToProps = (state, ownProps) => {
  let page = ownProps.match.params.page;
  if (page) {
    page = parseInt(page, 10);
  } else {
    page = 1;
  }
  const userEntries = getUsersFromState(state);
  let error = null;
  let loading = false;
  let canAddUsers = false;
  if (state.users && state.users.list) {
    error = state.users.list.error;
    canAddUsers = state.users.list.userCreatePermission;
    loading = state.users.list.loading;
  }
  const list = selectList(state);
  return {
    userEntries,
    error,
    loading,
    canAddUsers,
    list,
    page
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUsersByPage: (page: number) => {
      dispatch(fetchUsersByPage(page));
    },
    fetchUsersByLink: (link: string) => {
      dispatch(fetchUsersByLink(link));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(Users));
