// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { History } from "history";
import type {Role, PagedCollection} from "@scm-manager/ui-types";
import {
  fetchRolesByPage,
  getRolesFromState,
  selectListAsCollection,
  isPermittedToCreateRoles,
  isFetchRolesPending,
  getFetchRolesFailure
} from "../modules/roles";
import {
  Title,
  Loading,
  Notification,
  LinkPaginator,
  urls,
  CreateButton
} from "@scm-manager/ui-components";
import RoleTable from "../components/table/RoleTable";
import { getRolesLink } from "../../modules/indexResource";

type Props = {
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
  location: any,

  // dispatch functions
  fetchRolesByPage: (link: string, page: number, filter?: string) => void
};

class GlobalPermissionRoles extends React.Component<Props> {
  componentDidMount() {
    const { fetchRolesByPage, rolesLink, page, location } = this.props;
    fetchRolesByPage(
      rolesLink,
      page,
      urls.getQueryStringFromLocation(location)
    );
  }

  render() {
    const { t, loading } = this.props;

    if (loading) {
      return <Loading />;
    }

    return (
      <div>
        <Title title={t("config.roles.title")} />
        {this.renderPermissionsTable()}
        {this.renderCreateButton()}
      </div>
    );
  }

  renderPermissionsTable() {
    const { roles, list, page, location, t } = this.props;
    if (roles && roles.length > 0) {
      return (
        <>
          <RoleTable roles={roles} />
          <LinkPaginator
            collection={list}
            page={page}
            filter={urls.getQueryStringFromLocation(location)}
          />
        </>
      );
    }
    return <Notification type="info">{t("config.roles.noPermissionRoles")}</Notification>;
  }

  renderCreateButton() {
    const { canAddRoles, t } = this.props;
    if (canAddRoles) {
      return (
        <CreateButton label={t("config.permissions.createButton")} link="/create" />
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
    fetchRolesByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchRolesByPage(link, page, filter));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("config")(GlobalPermissionRoles));
