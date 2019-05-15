// @flow
import React from "react";
import { connect } from "react-redux";
import {withRouter} from "react-router-dom";
import { translate } from "react-i18next";
import type { History } from "history";
import type { Role, PagedCollection } from "@scm-manager/ui-types";
import {
  Title,
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
import PermissionRoleTable from "../components/table/PermissionRoleTable";
import { getRolesLink } from "../../modules/indexResource";
type Props = {
  baseUrl: string,
  roles: Role[],
  loading: boolean,
  error: Error,
  canAddRoles: boolean,
  list: PagedCollection,
  page: number,
  rolesLink: string,

  // context objects
  t: string => string,
  history: History,

  // dispatch functions
  fetchRolesByPage: (link: string, page: number) => void
};

class GlobalPermissionRoles extends React.Component<Props> {
  componentDidMount() {
    const { fetchRolesByPage, rolesLink, page } = this.props;
    fetchRolesByPage(rolesLink, page);
  }

  render() {
    const { t, loading } = this.props;

    if (loading) {
      return <Loading />;
    }

    return (
      <div>
        <Title title={t("roles.title")} />
        {this.renderPermissionsTable()}
        {this.renderCreateButton()}
      </div>
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
      <Notification type="info">{t("roles.noPermissionRoles")}</Notification>
    );
  }

  renderCreateButton() {
    const { canAddRoles, baseUrl, t } = this.props;
    if (canAddRoles) {
      return <CreateButton label={t("roles.createButton")} link={`${baseUrl}/create`} />;
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
  const rolesLink = getRolesLink(state);

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

export default withRouter(connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("config")(GlobalPermissionRoles)));
