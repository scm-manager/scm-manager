//@flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import {
  fetchAvailablePermissionsIfNeeded,
  fetchPermissions,
  getFetchAvailablePermissionsFailure,
  getAvailablePermissions,
  getFetchPermissionsFailure,
  isFetchAvailablePermissionsPending,
  isFetchPermissionsPending,
  getPermissionsOfRepo,
  hasCreatePermission,
  createPermission,
  isCreatePermissionPending,
  getCreatePermissionFailure,
  createPermissionReset,
  getDeletePermissionsFailure,
  getModifyPermissionsFailure,
  modifyPermissionReset,
  deletePermissionReset, getAvailableRepositoryRoles, getAvailableRepositoryVerbs
} from "../modules/permissions";
import {
  Loading,
  ErrorPage,
  Subtitle,
  LabelWithHelpIcon
} from "@scm-manager/ui-components";
import type {
  Permission,
  PermissionCollection,
  PermissionCreateEntry,
  RepositoryRole
} from "@scm-manager/ui-types";
import SinglePermission from "./SinglePermission";
import CreatePermissionForm from "./CreatePermissionForm";
import type { History } from "history";
import { getPermissionsLink } from "../../modules/repos";
import {
  getGroupAutoCompleteLink, getRepositoryRolesLink, getRepositoryVerbsLink,
  getUserAutoCompleteLink
} from "../../../modules/indexResource";

type Props = {
  availableRepositoryRoles: RepositoryRole[],
  availableVerbs: string[],
  namespace: string,
  repoName: string,
  loading: boolean,
  error: Error,
  permissions: PermissionCollection,
  hasPermissionToCreate: boolean,
  loadingCreatePermission: boolean,
  repositoryRolesLink: string,
  repositoryVerbsLink: string,
  permissionsLink: string,
  groupAutoCompleteLink: string,
  userAutoCompleteLink: string,

  //dispatch functions
  fetchAvailablePermissionsIfNeeded: () => void,
  fetchPermissions: (link: string, namespace: string, repoName: string) => void,
  createPermission: (
    link: string,
    permission: PermissionCreateEntry,
    namespace: string,
    repoName: string,
    callback?: () => void
  ) => void,
  createPermissionReset: (string, string) => void,
  modifyPermissionReset: (string, string) => void,
  deletePermissionReset: (string, string) => void,
  // context props
  t: string => string,
  match: any,
  history: History
};


class Permissions extends React.Component<Props> {
  componentDidMount() {
    const {
      fetchAvailablePermissionsIfNeeded,
      fetchPermissions,
      namespace,
      repoName,
      modifyPermissionReset,
      createPermissionReset,
      deletePermissionReset,
      permissionsLink,
      repositoryRolesLink,
      repositoryVerbsLink
    } = this.props;

    createPermissionReset(namespace, repoName);
    modifyPermissionReset(namespace, repoName);
    deletePermissionReset(namespace, repoName);
    fetchAvailablePermissionsIfNeeded(repositoryRolesLink, repositoryVerbsLink);
    fetchPermissions(permissionsLink, namespace, repoName);
  }

  createPermission = (permission: Permission) => {
    this.props.createPermission(
      this.props.permissionsLink,
      permission,
      this.props.namespace,
      this.props.repoName
    );
  };

  render() {
    const {
      availablePermissions,
      availableRepositoryRoles,
      availableVerbs,
      loading,
      error,
      permissions,
      t,
      namespace,
      repoName,
      loadingCreatePermission,
      hasPermissionToCreate,
      userAutoCompleteLink,
      groupAutoCompleteLink
    } = this.props;
    if (error) {
      return (
        <ErrorPage
          title={t("permission.error-title")}
          subtitle={t("permission.error-subtitle")}
          error={error}
        />
      );
    }

    if (loading || !permissions || !availablePermissions) {
      return <Loading />;
    }

    const createPermissionForm = hasPermissionToCreate ? (
      <CreatePermissionForm
        availableRoles={availableRepositoryRoles}
        availableVerbs={availableVerbs}
        createPermission={permission => this.createPermission(permission)}
        loading={loadingCreatePermission}
        currentPermissions={permissions}
        userAutoCompleteLink={userAutoCompleteLink}
        groupAutoCompleteLink={groupAutoCompleteLink}
      />
    ) : null;

    return (
      <div>
        <Subtitle subtitle={t("permission.title")} />
        <table className="has-background-light table is-hoverable is-fullwidth">
          <thead>
            <tr>
              <th>
                <LabelWithHelpIcon
                  label={t("permission.name")}
                  helpText={t("permission.help.nameHelpText")}
                />
              </th>
              <th>
                <LabelWithHelpIcon
                  label={t("permission.role")}
                  helpText={t("permission.help.roleHelpText")}
                />
              </th>
              <th>
                <LabelWithHelpIcon
                  label={t("permission.permissions")}
                  helpText={t("permission.help.permissionsHelpText")}
                />
              </th>
              <th />
            </tr>
          </thead>
          <tbody>
            {permissions.map(permission => {
              return (
                <SinglePermission
                  availableRepositoryRoles={availableRepositoryRoles}
                  availableRepositoryVerbs={availableVerbs}
                  key={permission.name + permission.groupPermission.toString()}
                  namespace={namespace}
                  repoName={repoName}
                  permission={permission}
                />
              );
            })}
          </tbody>
        </table>
        {createPermissionForm}
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const namespace = ownProps.namespace;
  const repoName = ownProps.repoName;
  const error =
    getFetchPermissionsFailure(state, namespace, repoName) ||
    getCreatePermissionFailure(state, namespace, repoName) ||
    getDeletePermissionsFailure(state, namespace, repoName) ||
    getModifyPermissionsFailure(state, namespace, repoName) ||
    getFetchAvailablePermissionsFailure(state);
  const loading =
    isFetchPermissionsPending(state, namespace, repoName) ||
    isFetchAvailablePermissionsPending(state);
  const permissions = getPermissionsOfRepo(state, namespace, repoName);
  const loadingCreatePermission = isCreatePermissionPending(
    state,
    namespace,
    repoName
  );
  const hasPermissionToCreate = hasCreatePermission(state, namespace, repoName);
  const repositoryRolesLink = getRepositoryRolesLink(state);
  const repositoryVerbsLink = getRepositoryVerbsLink(state);
  const permissionsLink = getPermissionsLink(state, namespace, repoName);
  const groupAutoCompleteLink = getGroupAutoCompleteLink(state);
  const userAutoCompleteLink = getUserAutoCompleteLink(state);
  const availablePermissions = getAvailablePermissions(state);
  const availableRepositoryRoles = getAvailableRepositoryRoles(state);
  const availableVerbs = getAvailableRepositoryVerbs(state);

  return {
    availablePermissions,
    availableRepositoryRoles,
    availableVerbs,
    namespace,
    repoName,
    repositoryRolesLink,
    repositoryVerbsLink,
    error,
    loading,
    permissions,
    hasPermissionToCreate,
    loadingCreatePermission,
    permissionsLink,
    groupAutoCompleteLink,
    userAutoCompleteLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchPermissions: (link: string, namespace: string, repoName: string) => {
      dispatch(fetchPermissions(link, namespace, repoName));
    },
    fetchAvailablePermissionsIfNeeded: (repositoryRolesLink: string, repositoryVerbsLink: string) => {
      dispatch(fetchAvailablePermissionsIfNeeded(repositoryRolesLink, repositoryVerbsLink));
    },
    createPermission: (
      link: string,
      permission: PermissionCreateEntry,
      namespace: string,
      repoName: string,
      callback?: () => void
    ) => {
      dispatch(
        createPermission(link, permission, namespace, repoName, callback)
      );
    },
    createPermissionReset: (namespace: string, repoName: string) => {
      dispatch(createPermissionReset(namespace, repoName));
    },
    modifyPermissionReset: (namespace: string, repoName: string) => {
      dispatch(modifyPermissionReset(namespace, repoName));
    },
    deletePermissionReset: (namespace: string, repoName: string) => {
      dispatch(deletePermissionReset(namespace, repoName));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(Permissions));
