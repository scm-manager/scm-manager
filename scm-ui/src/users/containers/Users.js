// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { History } from "history";
import queryString from "query-string";
import type { User, PagedCollection } from "@scm-manager/ui-types";
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
  Notification,
  LinkPaginator,
  getPageFromMatch
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
  fetchUsersByPage: (link: string, page: number, filter?: any) => void,
  fetchUsersByLink: (link: string) => void
};

type State = {
  page: number
};

class Users extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      page: -1
    };
  }

  componentDidMount() {
    const { fetchUsersByPage, usersLink, page } = this.props;
    fetchUsersByPage(usersLink, page, this.getQueryString());
    this.setState({ page: page });
  }

  componentDidUpdate = (prevProps: Props) => {
    const { list, page, location, fetchUsersByPage, usersLink } = this.props;
    if (list && page) {
      if (
        page !== this.state.page ||
        prevProps.location.search !== location.search
      ) {
        fetchUsersByPage(usersLink, page, this.getQueryString());
        this.setState({ page: page });
      }
    }
  };

  render() {
    const { users, loading, error, history, t } = this.props;
    return (
      <Page
        title={t("users.title")}
        subtitle={t("users.subtitle")}
        loading={loading || !users}
        error={error}
        filter={filter => {
          history.push("/users/?q=" + filter);
        }}
      >
        {this.renderUserTable()}
        {this.renderCreateButton()}
        {this.renderPageActionCreateButton()}
      </Page>
    );
  }

  renderUserTable() {
    const { users, t } = this.props;
    if (users && users.length > 0) {
      return (
        <>
          <UserTable users={users} />
          {this.renderPaginator()}
        </>
      );
    }
    return <Notification type="info">{t("users.noUsers")}</Notification>;
  }

  renderPaginator = () => {
    const { list, page } = this.props;
    if (list) {
      return (
        <LinkPaginator
          collection={list}
          page={page}
          filter={this.getQueryString()}
        />
      );
    }
    return null;
  };

  renderCreateButton() {
    const { canAddUsers, t } = this.props;
    if (canAddUsers) {
      return <CreateButton label={t("users.createButton")} link="/users/add" />;
    }
    return null;
  }

  renderPageActionCreateButton() {
    const { canAddUsers, t } = this.props;
    if (canAddUsers) {
      return (
        <PageActions>
          <Button
            label={t("users.createButton")}
            link="/users/add"
            color="primary"
          />
        </PageActions>
      );
    }

    return null;
  }

  getQueryString = () => {
    const { location } = this.props;
    return location.search ? queryString.parse(location.search).q : null;
  };
}

const mapStateToProps = (state, ownProps) => {
  const { match } = ownProps;
  const users = getUsersFromState(state);
  const loading = isFetchUsersPending(state);
  const error = getFetchUsersFailure(state);
  const page = getPageFromMatch(match);
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
    fetchUsersByPage: (link: string, page: number, filter?: any) => {
      dispatch(fetchUsersByPage(link, page, filter));
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
