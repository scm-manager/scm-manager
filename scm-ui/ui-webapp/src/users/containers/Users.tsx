/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { RouteComponentProps } from "react-router-dom";
import { PagedCollection, User } from "@scm-manager/ui-types";
import {
  CreateButton,
  LinkPaginator,
  Notification,
  OverviewPageActions,
  Page,
  PageActions,
  urls
} from "@scm-manager/ui-components";
import { getUsersLink } from "../../modules/indexResource";
import {
  fetchUsersByPage,
  getFetchUsersFailure,
  getUsersFromState,
  isFetchUsersPending,
  isPermittedToCreateUsers,
  selectListAsCollection
} from "../modules/users";
import { UserTable } from "./../components/table";

type Props = RouteComponentProps &
  WithTranslation & {
    users: User[];
    loading: boolean;
    error: Error;
    canAddUsers: boolean;
    list: PagedCollection;
    page: number;
    usersLink: string;

    // dispatch functions
    fetchUsersByPage: (link: string, page: number, filter?: string) => void;
  };

class Users extends React.Component<Props> {
  componentDidMount() {
    const { fetchUsersByPage, usersLink, page, location } = this.props;
    fetchUsersByPage(usersLink, page, urls.getQueryStringFromLocation(location));
  }

  componentDidUpdate = (prevProps: Props) => {
    const { loading, list, page, usersLink, location, fetchUsersByPage } = this.props;
    if (list && page && !loading) {
      const statePage: number = this.resolveStatePage();
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchUsersByPage(usersLink, page, urls.getQueryStringFromLocation(location));
      }
    }
  };

  resolveStatePage = (props = this.props) => {
    const { list } = props;
    if (list.page) {
      return list.page + 1;
    }
    // set page to 1 if undefined, because if users couldn't be fetched it would lead to a fetch-loop otherwise
    return 1;
  };

  render() {
    const { users, loading, error, canAddUsers, t } = this.props;

    return (
      <Page title={t("users.title")} subtitle={t("users.subtitle")} loading={loading || !users} error={error}>
        {this.renderUserTable()}
        {this.renderCreateButton()}
        <PageActions>
          <OverviewPageActions showCreateButton={canAddUsers} link="users" label={t("users.createButton")} />
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
          <LinkPaginator collection={list} page={page} filter={urls.getQueryStringFromLocation(location)} />
        </>
      );
    }
    return <Notification type="info">{t("users.noUsers")}</Notification>;
  }

  renderCreateButton() {
    const { canAddUsers, t } = this.props;
    if (canAddUsers) {
      return <CreateButton label={t("users.createButton")} link="/users/create" />;
    }
    return null;
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
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

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchUsersByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchUsersByPage(link, page, filter));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("users")(Users));
