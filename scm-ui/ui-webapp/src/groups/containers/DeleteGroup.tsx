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
import { Group } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { deleteGroup, getDeleteGroupFailure, isDeleteGroupPending } from "../modules/groups";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  group: Group;
  confirmDialog?: boolean;
  deleteGroup: (group: Group, callback?: () => void) => void;

  // context props
  history: History;
};

export const DeleteGroup: FC<Props> = ({ confirmDialog = true, group, history, t, deleteGroup, loading, error }) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);

  const deleteGroupCallback = () => {
    deleteGroup(group, groupDeleted);
  };

  const groupDeleted = () => {
    history.push("/groups/");
  };

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const isDeletable = () => {
    return group._links.delete;
  };

  const action = confirmDialog ? confirmDelete : deleteGroupCallback;

  if (!isDeletable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("deleteGroup.confirmAlert.title")}
        message={t("deleteGroup.confirmAlert.message")}
        buttons={[
          {
            className: "is-outlined",
            label: t("deleteGroup.confirmAlert.submit"),
            onClick: () => deleteGroupCallback()
          },
          {
            label: t("deleteGroup.confirmAlert.cancel"),
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
      <Level right={<DeleteButton label={t("deleteGroup.button")} action={action} loading={loading} />} />
    </>
  );
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isDeleteGroupPending(state, ownProps.group.name);
  const error = getDeleteGroupFailure(state, ownProps.group.name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    deleteGroup: (group: Group, callback?: () => void) => {
      dispatch(deleteGroup(group, callback));
    }
  };
};

export default compose(
  connect(mapStateToProps, mapDispatchToProps),
  withRouter,
  withTranslation("groups")
)(DeleteGroup);
