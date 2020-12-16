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
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Button, ConfirmAlert, ErrorNotification, Level } from "@scm-manager/ui-components";
import { getModifyRepoFailure, isModifyRepoPending, unarchiveRepo } from "../modules/repos";

type Props = {
  loading: boolean;
  error: Error;
  repository: Repository;
  confirmDialog?: boolean;
  unarchiveRepo: (p1: Repository, p2: () => void) => void;
};

const UnarchiveRepo: FC<Props> = ({ confirmDialog = true, repository, unarchiveRepo, loading, error }: Props) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");

  const unarchived = () => {
    window.location.reload();
  };

  const unarchiveRepoCallback = () => {
    unarchiveRepo(repository, unarchived);
  };

  const confirmUnarchive = () => {
    setShowConfirmAlert(true);
  };

  const isUnarchiveable = () => {
    return repository._links.unarchive;
  };

  const action = confirmDialog ? confirmUnarchive : unarchiveRepoCallback;

  if (!isUnarchiveable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("unarchiveRepo.confirmAlert.title")}
        message={t("unarchiveRepo.confirmAlert.message")}
        buttons={[
          {
            className: "is-outlined",
            label: t("unarchiveRepo.confirmAlert.submit"),
            onClick: () => unarchiveRepoCallback(),
          },
          {
            label: t("unarchiveRepo.confirmAlert.cancel"),
            onClick: () => null,
          },
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
            <strong>{t("unarchiveRepo.subtitle")}</strong>
            <br />
            {t("unarchiveRepo.description")}
          </p>
        }
        right={
          <Button color="warning" icon="box-open" label={t("unarchiveRepo.button")} action={action} loading={loading} />
        }
      />
    </>
  );
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isModifyRepoPending(state, namespace, name);
  const error = getModifyRepoFailure(state, namespace, name);
  return {
    loading,
    error,
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    unarchiveRepo: (repo: Repository, callback: () => void) => {
      dispatch(unarchiveRepo(repo, callback));
    },
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(UnarchiveRepo);
