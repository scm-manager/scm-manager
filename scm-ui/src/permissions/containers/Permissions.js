//@flow
import React from "react";
import connect from "react-redux/es/connect/connect";
import { translate } from "react-i18next";
import {
  fetchPermissions,
  getFetchPermissionsFailure,
  isFetchPermissionsPending,
  getPermissionsOfRepo
} from "../modules/permissions";
import Loading from "../../components/Loading";
import ErrorPage from "../../components/ErrorPage";
import type { PermissionCollection } from "../types/Permissions";
import PermissionsTable from "../components/table/PermissionsTable";

type Props = {
  namespace: string,
  name: string,
  loading: boolean,
  error: Error,
  permissions: PermissionCollection,

  //dispatch functions
  fetchPermissions: (namespace: string, name: string) => void,

  // context props
  t: string => string,
  match: any
};

class Permissions extends React.Component<Props> {
  componentDidMount() {
    const { fetchPermissions, namespace, name } = this.props;

    fetchPermissions(namespace, name);
  }

  render() {
    const { loading, error, permissions, t } = this.props;
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

    if (permissions.length > 0)
      return <PermissionsTable permissions={permissions} />;

    return <div />;
  }
}

const mapStateToProps = (state, ownProps) => {
  const namespace = ownProps.namespace;
  const name = ownProps.name;
  const error = getFetchPermissionsFailure(state, namespace, name);
  const loading = isFetchPermissionsPending(state, namespace, name);
  const permissions = getPermissionsOfRepo(state, namespace, name);
  return {
    namespace,
    name,
    error,
    loading,
    permissions
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchPermissions: (namespace: string, name: string) => {
      dispatch(fetchPermissions(namespace, name));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("permissions")(Permissions));
