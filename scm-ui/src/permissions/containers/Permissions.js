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
import type { History } from "history";
import Loading from "../../components/Loading";
import ErrorPage from "../../components/ErrorPage";

type Props = {
  namespace: string,
  name: string,
  loading: boolean,
  error: Error,
  permissions: Permissions,

  //dispatch functions
  fetchPermissions: (namespace: string, name: string) => void,

  // context props
  t: string => string,
  history: History,
  match: any
};

class Permissions extends React.Component<Props> {
  componentDidMount() {
    const { fetchPermissions, namespace, name } = this.props;

    fetchPermissions(namespace, name);
  }

  render() {
    const { namespace, name, loading, error, permissions, t } = this.props;

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

    return <div>Permissions will be shown here!</div>;
  }
}

const mapStateToProps = (state, ownProps) => {
  const namespace = ownProps.namespace;
  const name = ownProps.name;
  const error = getFetchPermissionsFailure(state);
  const loading = isFetchPermissionsPending(state);
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
