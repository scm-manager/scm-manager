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
import { RouteComponentProps, withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { PagedCollection, RepositoryRole } from "@scm-manager/ui-types";
import { CreateButton, LinkPaginator, Loading, Notification, Subtitle, Title, urls, ErrorNotification } from "@scm-manager/ui-components";
import {
  fetchRolesByPage,
  getFetchRolesFailure,
  getRolesFromState,
  isFetchRolesPending,
  isPermittedToCreateRoles,
  selectListAsCollection
} from "../modules/roles";
import PermissionRoleTable from "../components/PermissionRoleTable";
import { getRepositoryRolesLink } from "../../../modules/indexResource";

type Props = RouteComponentProps &
  WithTranslation & {
    baseUrl: string;
    roles: RepositoryRole[];
    loading: boolean;
    error: Error;
    canAddRoles: boolean;
    list: PagedCollection;
    page: number;
    rolesLink: string;

    // dispatch functions
    fetchRolesByPage: (link: string, page: number) => void;
  };

class RepositoryRoles extends React.Component<Props> {
  componentDidMount() {
    const { fetchRolesByPage, rolesLink, page } = this.props;
    fetchRolesByPage(rolesLink, page);
  }

  componentDidUpdate = (prevProps: Props) => {
    const { loading, list, page, rolesLink, location, fetchRolesByPage } = this.props;
    if (list && page && !loading) {
      const statePage: number = list.page + 1;
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchRolesByPage(rolesLink, page);
      }
    }
  };

  render() {
    const { t, loading, error } = this.props;

    if (error) {
      return <ErrorNotification />;
    }

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        <Title title={t("repositoryRole.title")} />
        <Subtitle subtitle={t("repositoryRole.overview.title")} />
        {this.renderPermissionsTable()}
        {this.renderCreateButton()}
      </>
    );
  }

  renderPermissionsTable() {
    const { baseUrl, roles, list, page, t } = this.props;
    if (roles && roles.length > 0) {
      return (
        <>
          <PermissionRoleTable baseUrl={baseUrl} roles={roles} />
          <LinkPaginator collection={list} page={page} />
        </>
      );
    }
    return <Notification type="info">{t("repositoryRole.overview.noPermissionRoles")}</Notification>;
  }

  renderCreateButton() {
    const { canAddRoles, baseUrl, t } = this.props;
    if (canAddRoles) {
      return <CreateButton label={t("repositoryRole.overview.createButton")} link={`${baseUrl}/create`} />;
    }
    return null;
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { match } = ownProps;
  const roles = getRolesFromState(state);
  const loading = isFetchRolesPending(state);
  const error = getFetchRolesFailure(state);
  const page = urls.getPageFromMatch(match);
  const canAddRoles = isPermittedToCreateRoles(state);
  const list = selectListAsCollection(state);
  const rolesLink = getRepositoryRolesLink(state);

  return {
    roles,
    loading,
    error,
    canAddRoles,
    list,
    page,
    rolesLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchRolesByPage: (link: string, page: number) => {
      dispatch(fetchRolesByPage(link, page));
    }
  };
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(withTranslation("admin")(RepositoryRoles)));
