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
  isPermittedToCreateUsers
} from "../modules/users";

import { Page } from "../../components/layout";
import { UserTable } from "./../components/table";
import type { UserEntry } from "../types/UserEntry";
import type { PageCollectionStateSlice } from "../../types/Collection";
import Paginator from "../../components/Paginator";
import CreateUserButton from "../components/buttons/CreateUserButton";

type Props = {
  userEntries: UserEntry[],
  canAddUsers: boolean,
  list: PageCollectionStateSlice,
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
    if (list.entry) {
      // backend starts paging by 0
      const statePage: number = list.entry.page + 1;
      if (page !== statePage) {
        this.props.history.push(`/users/${statePage}`);
      }
    }
  };

  render() {
    const { userEntries, list, t } = this.props;
    return (
      <Page
        title={t("users.title")}
        subtitle={t("users.subtitle")}
        loading={list.loading || !userEntries}
        error={list.error}
      >
        <UserTable entries={userEntries} />
        {this.renderPaginator()}
        {this.renderCreateButton()}
      </Page>
    );
  }

  renderPaginator() {
    const { list } = this.props;
    if (list.entry) {
      return (
        <Paginator collection={list.entry} onPageChange={this.onPageChange} />
      );
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
  const page = getPageFromProps(ownProps);
  const userEntries = getUsersFromState(state);
  const canAddUsers = isPermittedToCreateUsers(state);
  const list = selectListAsCollection(state);
  return {
    userEntries,
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
