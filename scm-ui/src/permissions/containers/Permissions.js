//@flow
import React from "react";
import connect from "react-redux/es/connect/connect";
import { translate } from "react-i18next";
import {
  fetchPermissions,
  getFetchPermissionsFailure,
  isFetchPermissionsPending,
  getPermissionsOfRepo,
  hasCreatePermission,
  createPermission,
  isCreatePermissionPending
} from "../modules/permissions";
import Loading from "../../components/Loading";
import ErrorPage from "../../components/ErrorPage";
import type { Permission, PermissionCollection } from "../types/Permissions";
import SinglePermission from "./SinglePermission";
import CreatePermissionForm from "../components/CreatePermissionForm";

type Props = {
  namespace: string,
  repoName: string,
  loading: boolean,
  error: Error,
  permissions: PermissionCollection,
  hasPermissionToCreate: boolean,
  loadingCreatePermission: boolean,

  //dispatch functions
  fetchPermissions: (namespace: string, repoName: string) => void,
  createPermission: (
    permission: Permission,
    namespace: string,
    repoName: string
  ) => void,

  // context props
  t: string => string,
  match: any
};

class Permissions extends React.Component<Props> {
  componentDidMount() {
    const { fetchPermissions, namespace, repoName } = this.props;

    fetchPermissions(namespace, repoName);
  }

  render() {
    const {
      loading,
      error,
      permissions,
      t,
      namespace,
      repoName,
      loadingCreatePermission,
      hasPermissionToCreate
    } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("permissions.error-title")}
          subtitle={t("permissions.error-subtitle")}
          error={error}
        />
      );
    }

    if (loading || !permissions) {
      return <Loading />;
    }

    const createPermissionForm = hasPermissionToCreate ? (
      <CreatePermissionForm
        createPermission={permission =>
          this.props.createPermission(permission, namespace, repoName)
        }
        loading={loadingCreatePermission}
      />
    ) : null;

    if (permissions.length > 0)
      return (
        <div>
          <table className="table is-hoverable is-fullwidth">
            <thead>
              <tr>
                <th>{t("permission.name")}</th>
                <th className="is-hidden-mobile">{t("permission.type")}</th>
                <th>{t("permission.group-permission")}</th>
              </tr>
            </thead>
            <tbody>
              {permissions.map((permission, index) => {
                return (
                  <SinglePermission
                    key={index}
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

    return <div />;
  }
}

const mapStateToProps = (state, ownProps) => {
  const namespace = ownProps.namespace;
  const repoName = ownProps.repoName;
  const error = getFetchPermissionsFailure(state, namespace, repoName);
  const loading = isFetchPermissionsPending(state, namespace, repoName);
  const permissions = getPermissionsOfRepo(state, namespace, repoName);
  const loadingCreatePermission = isCreatePermissionPending(
    state,
    namespace,
    repoName
  );
  console.log(permissions);
  const hasPermissionToCreate = hasCreatePermission(state, namespace, repoName);
  return {
    namespace,
    repoName,
    error,
    loading,
    permissions,
    hasPermissionToCreate,
    loadingCreatePermission
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchPermissions: (namespace: string, repoName: string) => {
      dispatch(fetchPermissions(namespace, repoName));
    },
    createPermission: (
      permission: Permission,
      namespace: string,
      repoName: string
    ) => {
      dispatch(createPermission(permission, namespace, repoName));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("permissions")(Permissions));
