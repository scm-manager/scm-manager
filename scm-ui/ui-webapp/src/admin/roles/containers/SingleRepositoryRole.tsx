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
import { Route, withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { RepositoryRole } from "@scm-manager/ui-types";
import { ErrorPage, Loading, Title } from "@scm-manager/ui-components";
import { mustGetRepositoryRolesLink } from "../../../modules/indexResource";
import { fetchRoleByName, getFetchRoleFailure, getRoleByName, isFetchRolePending } from "../modules/roles";
import PermissionRoleDetail from "../components/PermissionRoleDetails";
import EditRepositoryRole from "./EditRepositoryRole";
import { compose } from "redux";
import { urls } from "@scm-manager/ui-components";

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

  matchedUrl = () => {
    return urls.stripEndingSlash(this.props.match.url);
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
  const repositoryRolesLink = mustGetRepositoryRolesLink(state);
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
