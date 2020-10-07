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
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { deleteRepo, getDeleteRepoFailure, isDeleteRepoPending } from "../modules/repos";

type Props = {
  loading: boolean;
  error: Error;
  repository: Repository;
  confirmDialog?: boolean;
  deleteRepo: (p1: Repository, p2: () => void) => void;
};

const DeleteRepo: FC<Props> = ({ confirmDialog = true, repository, deleteRepo, loading, error }: Props) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");
  const history = useHistory();

  const deleted = () => {
    history.push("/repos/");
  };

  const deleteRepoCallback = () => {
    deleteRepo(repository, deleted);
  };

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const isDeletable = () => {
    return repository._links.delete;
  };

  const action = confirmDialog ? confirmDelete : deleteRepoCallback;

  if (!isDeletable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("deleteRepo.confirmAlert.title")}
        message={t("deleteRepo.confirmAlert.message")}
        buttons={[
          {
            className: "is-outlined",
            label: t("deleteRepo.confirmAlert.submit"),
            onClick: () => deleteRepoCallback()
          },
          {
            label: t("deleteRepo.confirmAlert.cancel"),
            onClick: () => null
          }
        ]}
        close={() => setShowConfirmAlert(false)}
      />
    );
  }

  return (
    <>
      <ErrorNotification error={error} />
      <Level
        left={
          <p>
            <strong>{t("deleteRepo.subtitle")}</strong>
            <br />
            {t("deleteRepo.description")}
          </p>
        }
        right={<DeleteButton label={t("deleteRepo.button")} action={action} loading={loading} />}
      />
    </>
  );
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isDeleteRepoPending(state, namespace, name);
  const error = getDeleteRepoFailure(state, namespace, name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    deleteRepo: (repo: Repository, callback: () => void) => {
      dispatch(deleteRepo(repo, callback));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(DeleteRepo);
