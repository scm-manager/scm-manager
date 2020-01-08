import React from "react";
import { connect } from "react-redux";
import { Route, withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { RepositoryRole } from "@scm-manager/ui-types";
import { Loading, ErrorPage, Title } from "@scm-manager/ui-components";
import { getRepositoryRolesLink } from "../../../modules/indexResource";
import { fetchRoleByName, getFetchRoleFailure, getRoleByName, isFetchRolePending } from "../modules/roles";
import PermissionRoleDetail from "../components/PermissionRoleDetails";
import EditRepositoryRole from "./EditRepositoryRole";
import { compose } from "redux";

type Props = WithTranslation & {
  roleName: string;
  role: RepositoryRole;
  loading: boolean;
  error: Error;
  repositoryRolesLink: string;
  disabled: boolean;

  // dispatcher function
  fetchRoleByName: (p1: string, p2: string) => void;

  // context objects
  match: any;
  history: History;
};

class SingleRepositoryRole extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchRoleByName(this.props.repositoryRolesLink, this.props.roleName);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { t, loading, error, role } = this.props;

    if (error) {
      return (
        <ErrorPage title={t("repositoryRole.errorTitle")} subtitle={t("repositoryRole.errorSubtitle")} error={error} />
      );
    }

    if (!role || loading) {
      return <Loading />;
    }

    const url = this.matchedUrl();

    const extensionProps = {
      role,
      url
    };

    return (
      <>
        <Title title={t("repositoryRole.title")} />
        <Route path={`${url}/info`} component={() => <PermissionRoleDetail role={role} url={url} />} />
        <Route
          path={`${url}/edit`}
          exact
          component={() => <EditRepositoryRole role={role} history={this.props.history} />}
        />
        <ExtensionPoint name="roles.route" props={extensionProps} renderAll={true} />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const roleName = ownProps.match.params.role;
  const role = getRoleByName(state, roleName);
  const loading = isFetchRolePending(state, roleName);
  const error = getFetchRoleFailure(state, roleName);
  const repositoryRolesLink = getRepositoryRolesLink(state);
  return {
    repositoryRolesLink,
    roleName,
    role,
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchRoleByName: (link: string, name: string) => {
      dispatch(fetchRoleByName(link, name));
    }
  };
};

export default compose(
  withRouter,
  connect(mapStateToProps, mapDispatchToProps),
  withTranslation("admin")
)(SingleRepositoryRole);
