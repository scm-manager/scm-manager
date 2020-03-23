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
import RepositoryRoleForm from "./RepositoryRoleForm";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { getModifyRoleFailure, isModifyRolePending, modifyRole } from "../modules/roles";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import { RepositoryRole } from "@scm-manager/ui-types";
import { History } from "history";
import DeleteRepositoryRole from "./DeleteRepositoryRole";
import { compose } from "redux";

type Props = WithTranslation & {
  disabled: boolean;
  role: RepositoryRole;
  repositoryRolesLink: string;
  loading?: boolean;
  error?: Error;

  // context objects
  history: History;

  //dispatch function
  updateRole: (role: RepositoryRole, callback?: () => void) => void;
};

class EditRepositoryRole extends React.Component<Props> {
  repositoryRoleUpdated = () => {
    this.props.history.push("/admin/roles/");
  };

  updateRepositoryRole = (role: RepositoryRole) => {
    this.props.updateRole(role, this.repositoryRoleUpdated);
  };

  render() {
    const { loading, error, t } = this.props;

    if (loading) {
      return <Loading />;
    } else if (error) {
      return <ErrorNotification error={error} />;
    }

    return (
      <>
        <Subtitle subtitle={t("repositoryRole.editSubtitle")} />
        <RepositoryRoleForm
          role={this.props.role}
          submitForm={(role: RepositoryRole) => this.updateRepositoryRole(role)}
        />
        <DeleteRepositoryRole role={this.props.role} />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isModifyRolePending(state, ownProps.role.name);
  const error = getModifyRoleFailure(state, ownProps.role.name);

  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    updateRole: (role: RepositoryRole, callback?: () => void) => {
      dispatch(modifyRole(role, callback));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("admin"))(EditRepositoryRole);
