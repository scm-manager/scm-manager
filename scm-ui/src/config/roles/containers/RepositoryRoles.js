// @flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { translate } from "react-i18next";
import type { History } from "history";
import type { RepositoryRole, PagedCollection } from "@scm-manager/ui-types";
import {
  Title,
  Subtitle,
  Loading,
  Notification,
  LinkPaginator,
  urls,
  CreateButton
} from "@scm-manager/ui-components";
import {
  fetchRolesByPage,
  getRolesFromState,
  selectListAsCollection,
  isPermittedToCreateRoles,
  isFetchRolesPending,
  getFetchRolesFailure
} from "../modules/roles";
import PermissionRoleTable from "../components/PermissionRoleTable";
import { getRepositoryRolesLink } from "../../../modules/indexResource";

type Props = {
  baseUrl: string,
  roles: RepositoryRole[],
  loading: boolean,
  error: Error,
  canAddRoles: boolean,
  list: PagedCollection,
  page: number,
  rolesLink: string,

  // context objects
  t: string => string,
  history: History,
  location: any,

  // dispatch functions
  fetchRolesByPage: (link: string, page: number) => void
};

class RepositoryRoles extends React.Component<Props> {
  componentDidMount() {
    const { fetchRolesByPage, rolesLink, page } = this.props;
    fetchRolesByPage(rolesLink, page);
  }

  componentDidUpdate = (prevProps: Props) => {
    const {
      loading,
      list,
      page,
      rolesLink,
      location,
      fetchRolesByPage
    } = this.props;
    if (list && page && !loading) {
      const statePage: number = list.page + 1;
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchRolesByPage(
          rolesLink,
          page
        );
      }
    }
  };

  render() {
    const { t, loading } = this.props;

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
    return (
      <Notification type="info">
        {t("repositoryRole.overview.noPermissionRoles")}
      </Notification>
    );
  }

  renderCreateButton() {
    const { canAddRoles, baseUrl, t } = this.props;
    if (canAddRoles) {
      return (
        <CreateButton
          label={t("repositoryRole.overview.createButton")}
          link={`${baseUrl}/create`}
        />
      );
    }
    return null;
  }
}

const mapStateToProps = (state, ownProps) => {
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

const mapDispatchToProps = dispatch => {
  return {
    fetchRolesByPage: (link: string, page: number) => {
      dispatch(fetchRolesByPage(link, page));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("config")(RepositoryRoles))
);
