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
import {
  createPermission,
  createPermissionReset,
  deletePermissionReset,
  fetchAvailablePermissionsIfNeeded,
  fetchPermissions,
  getAvailablePermissions,
  getAvailableRepositoryRoles,
  getAvailableRepositoryVerbs,
  getCreatePermissionFailure,
  getDeletePermissionsFailure,
  getFetchAvailablePermissionsFailure,
  getFetchPermissionsFailure,
  getModifyPermissionsFailure,
  getPermissionsOfRepo,
  hasCreatePermission,
  isCreatePermissionPending,
  isFetchAvailablePermissionsPending,
  isFetchPermissionsPending,
  modifyPermissionReset
} from "../modules/permissions";
import { ErrorPage, LabelWithHelpIcon, Loading, Subtitle } from "@scm-manager/ui-components";
import { Permission, PermissionCollection, PermissionCreateEntry, RepositoryRole } from "@scm-manager/ui-types";
import SinglePermission from "./SinglePermission";
import CreatePermissionForm from "./CreatePermissionForm";
import { History } from "history";
import { getPermissionsLink } from "../../modules/repos";
import {
  getGroupAutoCompleteLink,
  getRepositoryRolesLink,
  getRepositoryVerbsLink,
  getUserAutoCompleteLink
} from "../../../modules/indexResource";
type Props = WithTranslation & {
  availablePermissions: boolean;
  availableRepositoryRoles: RepositoryRole[];
  availableVerbs: string[];
  namespace: string;
  repoName?: string;
  loading: boolean;
  error: Error;
  permissions: PermissionCollection;
  hasPermissionToCreate: boolean;
  loadingCreatePermission: boolean;
  repositoryRolesLink: string;
  repositoryVerbsLink: string;
  permissionsLink: string;
  groupAutocompleteLink: string;
  userAutocompleteLink: string;

  // dispatch functions
  fetchAvailablePermissionsIfNeeded: (repositoryRolesLink: string, repositoryVerbsLink: string) => void;
  fetchPermissions: (link: string, namespace: string, repoName?: string) => void;
  createPermission: (
    link: string,
    permission: PermissionCreateEntry,
    namespace: string,
    repoName?: string,
    callback?: () => void
  ) => void;
  createPermissionReset: (namespace: string, repoName?: string) => void;
  modifyPermissionReset: (namespace: string, repoName?: string) => void;
  deletePermissionReset: (namespace: string, repoName?: string) => void;

  // context props
  match: any;
  history: History;
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
    this.props.createPermission(this.props.permissionsLink, permission, this.props.namespace, this.props.repoName);
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
      userAutocompleteLink,
      groupAutocompleteLink
    } = this.props;
    if (error) {
      return <ErrorPage title={t("permission.error-title")} subtitle={t("permission.error-subtitle")} error={error} />;
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
        userAutocompleteLink={userAutocompleteLink}
        groupAutocompleteLink={groupAutocompleteLink}
      />
    ) : null;

    return (
      <div>
        <Subtitle subtitle={t("permission.title")} />
        <table className="card-table table is-hoverable is-fullwidth">
          <thead>
            <tr>
              <th>
                <LabelWithHelpIcon label={t("permission.name")} helpText={t("permission.help.nameHelpText")} />
              </th>
              <th>
                <LabelWithHelpIcon label={t("permission.role")} helpText={t("permission.help.roleHelpText")} />
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

const mapStateToProps = (state: any, ownProps: Props) => {
  const namespace = ownProps.namespace;
  const repoName = ownProps.repoName;
  const error =
    getFetchPermissionsFailure(state, namespace, repoName) ||
    getCreatePermissionFailure(state, namespace, repoName) ||
    getDeletePermissionsFailure(state, namespace, repoName) ||
    getModifyPermissionsFailure(state, namespace, repoName) ||
    getFetchAvailablePermissionsFailure(state);
  const loading = isFetchPermissionsPending(state, namespace, repoName) || isFetchAvailablePermissionsPending(state);
  const permissions = getPermissionsOfRepo(state, namespace, repoName);
  const loadingCreatePermission = isCreatePermissionPending(state, namespace, repoName);
  const hasPermissionToCreate = hasCreatePermission(state, namespace, repoName);
  const repositoryRolesLink = getRepositoryRolesLink(state);
  const repositoryVerbsLink = getRepositoryVerbsLink(state);
  const permissionsLink = getPermissionsLink(state, namespace, repoName);
  const groupAutocompleteLink = getGroupAutoCompleteLink(state);
  const userAutocompleteLink = getUserAutoCompleteLink(state);
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
    groupAutocompleteLink,
    userAutocompleteLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchPermissions: (link: string, namespace: string, repoName?: string) => {
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
      dispatch(createPermission(link, permission, namespace, repoName, callback));
    },
    createPermissionReset: (namespace: string, repoName?: string) => {
      dispatch(createPermissionReset(namespace, repoName));
    },
    modifyPermissionReset: (namespace: string, repoName?: string) => {
      dispatch(modifyPermissionReset(namespace, repoName));
    },
    deletePermissionReset: (namespace: string, repoName?: string) => {
      dispatch(deletePermissionReset(namespace, repoName));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("repos")(Permissions));
