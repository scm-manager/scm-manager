//@flow
import React from "react";
import { connect } from "react-redux";
import { Redirect, Route, Switch, withRouter } from "react-router-dom";
import type {Role} from "@scm-manager/ui-types";
import {ErrorNotification, Loading} from "@scm-manager/ui-components";
import { getRolesLink } from "../../../modules/indexResource";
import {
  fetchRoleByName,
  getRoleByName,
  isFetchRolePending,
  getFetchRoleFailure
} from "../modules/roles";
import PermissionRoleDetail from "../components/PermissionRoleDetail";

type Props = {
  roleLink: string,
  roleName: string,
  role: Role,
  loading: boolean,
  error: Error,

  // context props
  match: any,
  t: string => string,

  // dispatch functions
  fetchRoleByName: (roleLink: string, roleName: string) => void
};

class PermissionRoleRoot extends React.Component<Props> {
  componentDidMount() {
    const { fetchRoleByName, roleLink, roleName } = this.props;
    fetchRoleByName(roleLink, roleName);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { loading, error, role} = this.props;

    const url = this.matchedUrl();

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading || !role) {
      return <Loading />;
    }

    return (
      <Switch>
        <Redirect exact from={url} to={`${url}/info`} />
        <Route
          path={`${url}/info`}
          component={() => (
            <PermissionRoleDetail role={role} />
          )}
        />
      </Switch>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const roleName = decodeURIComponent(ownProps.match.params.role);
  const role = getRoleByName(state, roleName);
  const loading = isFetchRolePending(state, roleName);
  const error = getFetchRoleFailure(state, roleName);
  const roleLink = getRolesLink(state);
  return {
    roleName,
    role,
    loading,
    error,
    roleLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRoleByName: (roleLink: string, roleName: string) => {
      dispatch(fetchRoleByName(roleLink, roleName));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(PermissionRoleRoot)
);
