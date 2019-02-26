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

import {
  Page,
  PageActions,
  Button,
  CreateButton,
  Paginator
} from "@scm-manager/ui-components";
import { UserTable } from "./../components/table";
import type { User, PagedCollection } from "@scm-manager/ui-types";
import { getUsersLink } from "../../modules/indexResource";

type Props = {
  users: User[],
  loading: boolean,
  error: Error,
  canAddUsers: boolean,
  list: PagedCollection,
  page: number,
  usersLink: string,

  // context objects
  t: string => string,
  history: History,

  // dispatch functions
  fetchUsersByPage: (link: string, page: number) => void,
  fetchUsersByLink: (link: string) => void
};

class Users extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUsersByPage(this.props.usersLink, this.props.page);
  }

  onPageChange = (link: string) => {
    this.props.fetchUsersByLink(link);
  };

  /**
   * reflect page transitions in the uri
   */
  componentDidUpdate() {
    const { page, list } = this.props;
    if (list && (list.page || list.page === 0)) {
      // backend starts paging by 0
      const statePage: number = list.page + 1;
      if (page !== statePage) {
        this.props.history.push(`/users/${statePage}`);
      }
    }
  }

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
        {this.renderPageActionCreateButton()}
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
    const { t } = this.props;
    if (this.props.canAddUsers) {
      return <CreateButton label={t("users.createButton")} link="/users/add" />;
    } else {
      return;
    }
  }

  renderPageActionCreateButton() {
    const { t } = this.props;
    if (this.props.canAddUsers) {
      return (
        <PageActions>
          <Button
            label={t("users.createButton")}
            link="/users/add"
            color="primary"
          />
        </PageActions>
      );
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

  const usersLink = getUsersLink(state);

  const page = getPageFromProps(ownProps);
  const canAddUsers = isPermittedToCreateUsers(state);
  const list = selectListAsCollection(state);

  return {
    users,
    loading,
    error,
    canAddUsers,
    list,
    page,
    usersLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUsersByPage: (link: string, page: number) => {
      dispatch(fetchUsersByPage(link, page));
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
