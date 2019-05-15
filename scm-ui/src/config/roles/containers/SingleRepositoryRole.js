//@flow
import React from "react";
import { connect } from "react-redux";
import {
  Page,
  Loading,
  ErrorPage
} from "@scm-manager/ui-components";
import { Route } from "react-router";
import type { History } from "history";
import { EditRepositoryRoleNavLink, RepositoryRoleDetailNavLink } from "../../components/navLinks";
import { translate } from "react-i18next";
import type { Role } from "@scm-manager/ui-types";
import {getRepositoryRolesLink} from "../../../modules/indexResource";
import {ExtensionPoint} from "@scm-manager/ui-extensions";
import {fetchRoleByName, getFetchRoleFailure, getRoleByName, isFetchRolePending} from "../modules/roles";

type Props = {
  name: string,
  role: Role,
  loading: boolean,
  error: Error,
  repositoryRolesLink: string,
  disabled: boolean,

  // dispatcher function
  fetchRoleByName: (string, string) => void,

  // context objects
  t: string => string,
  match: any,
  history: History
};

class SingleRepositoryRole extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchRoleByName(this.props.repositoryRolesLink, this.props.name);
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
        <ErrorPage
          title={t("singleUser.errorTitle")}
          subtitle={t("singleUser.errorSubtitle")}
          error={error}
        />
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
      <Page title={role.displayName}>
        <div className="columns">
          <div className="column is-three-quarters">
            <Route path={url} exact component={() => <RepositoryRoleDetailNavLink role={role} />} />
            <Route
              path={`${url}/settings/general`}
              component={() => <EditRepositoryRoleNavLink role={role} />}
            />
            <ExtensionPoint
              name="user.route"
              props={extensionProps}
              renderAll={true}
            />
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const name = ownProps.match.params.name;
  const role = getRoleByName(state, name);
  const loading = isFetchRolePending(state, name);
  const error = getFetchRoleFailure(state, name);
  const repositoryRolesLink = getRepositoryRolesLink(state);
  return {
    repositoryRolesLink,
    name,
    role,
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRoleByName: (link: string, name: string) => {
      dispatch(fetchRoleByName(link, name));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(SingleRepositoryRole));
