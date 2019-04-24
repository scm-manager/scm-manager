// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { History } from "history";
import type { User, PagedCollection } from "@scm-manager/ui-types";
import {
  fetchUsersByPage,
  getUsersFromState,
  selectListAsCollection,
  isPermittedToCreateUsers,
  isFetchUsersPending,
  getFetchUsersFailure
} from "../modules/users";
import {
  Page,
  PageActions,
  OverviewPageActions,
  Notification,
  LinkPaginator,
  urls,
  CreateButton
} from "@scm-manager/ui-components";
import { UserTable } from "./../components/table";
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
  location: any,

  // dispatch functions
  fetchUsersByPage: (link: string, page: number, filter?: string) => void
};

class Users extends React.Component<Props> {
  componentDidMount() {
    const { fetchUsersByPage, usersLink, page, location } = this.props;
    fetchUsersByPage(
      usersLink,
      page,
      urls.getQueryStringFromLocation(location)
    );
  }

  componentDidUpdate = (prevProps: Props) => {
    const {
      loading,
      list,
      page,
      usersLink,
      location,
      fetchUsersByPage
    } = this.props;
    if (list && page && !loading) {
      const statePage: number = list.page + 1;
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchUsersByPage(
          usersLink,
          page,
          urls.getQueryStringFromLocation(location)
        );
      }
    }
  };

  render() {
    const { users, loading, error, canAddUsers, t } = this.props;
    return (
      <Page
        title={t("users.title")}
        subtitle={t("users.subtitle")}
        loading={loading || !users}
        error={error}
      >
        {this.renderUserTable()}
        {this.renderCreateButton()}
        <PageActions>
          <OverviewPageActions
            showCreateButton={canAddUsers}
            link="users"
            label={t("users.createButton")}
          />
        </PageActions>
      </Page>
    );
  }

  renderUserTable() {
    const { users, list, page, location, t } = this.props;
    if (users && users.length > 0) {
      return (
        <>
          <UserTable users={users} />
          <LinkPaginator
            collection={list}
            page={page}
            filter={urls.getQueryStringFromLocation(location)}
          />
        </>
      );
    }
    return <Notification type="info">{t("users.noUsers")}</Notification>;
  }

  renderCreateButton() {
    const { canAddUsers, t } = this.props;
    if (canAddUsers) {
      return (
        <CreateButton label={t("users.createButton")} link="/users/create" />
      );
    }
    return null;
  }
}

const mapStateToProps = (state, ownProps) => {
  const { match } = ownProps;
  const users = getUsersFromState(state);
  const loading = isFetchUsersPending(state);
  const error = getFetchUsersFailure(state);
  const page = urls.getPageFromMatch(match);
  const canAddUsers = isPermittedToCreateUsers(state);
  const list = selectListAsCollection(state);
  const usersLink = getUsersLink(state);

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
    fetchUsersByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchUsersByPage(link, page, filter));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(Users));
