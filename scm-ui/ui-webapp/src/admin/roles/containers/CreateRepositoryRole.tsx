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
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { RepositoryRole } from "@scm-manager/ui-types";
import { ErrorNotification, Subtitle, Title } from "@scm-manager/ui-components";
import { createRole, getCreateRoleFailure, getFetchVerbsFailure, isFetchVerbsPending } from "../modules/roles";
import { getRepositoryRolesLink, getRepositoryVerbsLink } from "../../../modules/indexResource";
import RepositoryRoleForm from "./RepositoryRoleForm";

type Props = WithTranslation & {
  repositoryRolesLink: string;
  error?: Error;
  history: History;

  // dispatch function
  addRole: (link: string, role: RepositoryRole, callback?: () => void) => void;
};

class CreateRepositoryRole extends React.Component<Props> {
  repositoryRoleCreated = (role: RepositoryRole) => {
    const { history } = this.props;
    history.push("/admin/role/" + role.name + "/info");
  };

  createRepositoryRole = (role: RepositoryRole) => {
    this.props.addRole(this.props.repositoryRolesLink, role, () => this.repositoryRoleCreated(role));
  };

  render() {
    const { t, error } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    return (
      <>
        <Title title={t("repositoryRole.title")} />
        <Subtitle subtitle={t("repositoryRole.createSubtitle")} />
        <RepositoryRoleForm submitForm={(role: RepositoryRole) => this.createRepositoryRole(role)} />
      </>
    );
  }
}

const mapStateToProps = (state: any) => {
  const loading = isFetchVerbsPending(state);
  const error = getFetchVerbsFailure(state) || getCreateRoleFailure(state);
  const verbsLink = getRepositoryVerbsLink(state);
  const repositoryRolesLink = getRepositoryRolesLink(state);

  return {
    loading,
    error,
    verbsLink,
    repositoryRolesLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    addRole: (link: string, role: RepositoryRole, callback?: () => void) => {
      dispatch(createRole(link, role, callback));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("admin"))(CreateRepositoryRole);
