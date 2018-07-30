// @flow
import React from "react";
import type { History } from "history";
import { connect } from "react-redux";
import { translate } from "react-i18next";

import {
  fetchUsersByPage,
  fetchUsersByLink,
  getUsersFromState,
  selectListAsCollection,
  isPermittedToCreateUsers,
  isFetchUsersPending,
  getFetchUsersFailure
} from "../modules/users";

import { Page } from "../../components/layout";
import { UserTable } from "./../components/table";
import type { User } from "../types/User";
import type { PagedCollection } from "../../types/Collection";
import Paginator from "../../components/Paginator";
import CreateUserButton from "../components/buttons/CreateUserButton";

type Props = {
  users: User[],
  loading: boolean,
  error: Error,
  canAddUsers: boolean,
  list: PagedCollection,
  page: number,

  // context objects
  t: string => string,
  history: History,

  // dispatch functions
  fetchUsersByPage: (page: number) => void,
  fetchUsersByLink: (link: string) => void
};

class Users extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUsersByPage(this.props.page);
  }

  onPageChange = (link: string) => {
    this.props.fetchUsersByLink(link);
  };

  /**
   * reflect page transitions in the uri
   */
  componentDidUpdate = (prevProps: Props) => {
    const { page, list } = this.props;
    if (list.page) {
      // backend starts paging by 0
      const statePage: number = list.page + 1;
      if (page !== statePage) {
        this.props.history.push(`/users/${statePage}`);
      }
    }
  };

  render() {
    const { users, loading, error, t } = this.props;
    return (
      <Page
        title={t("users.title")}
        subtitle={t("users.subtitle")}
        loading={loading || !users}
        error={error}
      >
        <UserTable users={users} />
        {this.renderPaginator()}
        {this.renderCreateButton()}
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

  renderCreateButton() {
    if (this.props.canAddUsers) {
      return <CreateUserButton />;
    } else {
      return;
    }
  }
}

const getPageFromProps = props => {
  let page = props.match.params.page;
  if (page) {
    page = parseInt(page, 10);
  } else {
    page = 1;
  }
  return page;
};

const mapStateToProps = (state, ownProps) => {
  const users = getUsersFromState(state);
  const loading = isFetchUsersPending(state);
  const error = getFetchUsersFailure(state);

  const page = getPageFromProps(ownProps);
  const canAddUsers = isPermittedToCreateUsers(state);
  const list = selectListAsCollection(state);

  return {
    users,
    loading,
    error,
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
