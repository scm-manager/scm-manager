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
import { archiveRepo, getModifyRepoFailure, isModifyRepoPending } from "../modules/repos";

type Props = {
  loading: boolean;
  error: Error;
  repository: Repository;
  confirmDialog?: boolean;
  archiveRepo: (p1: Repository, p2: () => void) => void;
};

const ArchiveRepo: FC<Props> = ({ confirmDialog = true, repository, archiveRepo, loading, error }: Props) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");

  const archived = () => {
    window.location.reload();
  };

  const archiveRepoCallback = () => {
    archiveRepo(repository, archived);
  };

  const confirmArchive = () => {
    setShowConfirmAlert(true);
  };

  const isArchiveable = () => {
    return repository._links.archive;
  };

  const action = confirmDialog ? confirmArchive : archiveRepoCallback;

  if (!isArchiveable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("archiveRepo.confirmAlert.title")}
        message={t("archiveRepo.confirmAlert.message")}
        buttons={[
          {
            className: "is-outlined",
            label: t("archiveRepo.confirmAlert.submit"),
            onClick: () => archiveRepoCallback(),
          },
          {
            label: t("archiveRepo.confirmAlert.cancel"),
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
            <strong>{t("archiveRepo.subtitle")}</strong>
            <br />
            {t("archiveRepo.description")}
          </p>
        }
        right={
          <Button color="warning" icon="archive" label={t("archiveRepo.button")} action={action} loading={loading} />
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
    archiveRepo: (repo: Repository, callback: () => void) => {
      dispatch(archiveRepo(repo, callback));
    },
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(ArchiveRepo);
