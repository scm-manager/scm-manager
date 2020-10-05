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
import React, { FC, useState } from "react";
import { connect } from "react-redux";
import { compose } from "redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { RepositoryRole } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { deleteRole, getDeleteRoleFailure, isDeleteRolePending } from "../modules/roles";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  role: RepositoryRole;
  confirmDialog?: boolean;
  deleteRole: (role: RepositoryRole, callback?: () => void) => void;

  // context props
  history: History;
};

const DeleteRepositoryRole: FC<Props> = ({
  confirmDialog = true,
  history,
  deleteRole,
  role,
  loading,
  error,
  t
}: Props) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);

  const roleDeleted = () => {
    history.push("/admin/roles/");
  };

  const deleteRoleCallback = () => {
    deleteRole(role, roleDeleted);
  };

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const isDeletable = () => {
    return role._links.delete;
  };

  const action = confirmDialog ? confirmDelete : deleteRoleCallback;

  if (!isDeletable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("repositoryRole.delete.confirmAlert.title")}
        message={t("repositoryRole.delete.confirmAlert.message")}
        buttons={[
          {
            className: "is-outlined",
            label: t("repositoryRole.delete.confirmAlert.submit"),
            onClick: () => deleteRoleCallback()
          },
          {
            label: t("repositoryRole.delete.confirmAlert.cancel"),
            onClick: () => null
          }
        ]}
        close={() => setShowConfirmAlert(false)}
      />
    );
  }

  return (
    <>
      <hr />
      <ErrorNotification error={error} />
      <Level right={<DeleteButton label={t("repositoryRole.delete.button")} action={action} loading={loading} />} />
    </>
  );
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isDeleteRolePending(state, ownProps.role.name);
  const error = getDeleteRoleFailure(state, ownProps.role.name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    deleteRole: (role: RepositoryRole, callback?: () => void) => {
      dispatch(deleteRole(role, callback));
    }
  };
};

export default compose(
  connect(mapStateToProps, mapDispatchToProps),
  withRouter,
  withTranslation("admin")
)(DeleteRepositoryRole);
