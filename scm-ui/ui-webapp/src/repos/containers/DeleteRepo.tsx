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
import React, { FC, useEffect, useState } from "react";
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useDeleteRepository } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  confirmDialog?: boolean;
};

const DeleteRepo: FC<Props> = ({ repository, confirmDialog = true }) => {
  const history = useHistory();
  const { isLoading, error, remove, isDeleted } = useDeleteRepository({
    onSuccess: () => {
      history.push("/repos/");
    }
  });
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");
  useEffect(() => {
    if (isDeleted) {
      setShowConfirmAlert(false);
    }
  }, [isDeleted]);

  const deleteRepoCallback = () => {
    remove(repository);
  };

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const action = confirmDialog ? confirmDelete : deleteRepoCallback;

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("deleteRepo.confirmAlert.title")}
        message={t("deleteRepo.confirmAlert.message")}
        buttons={[
          {
            label: t("deleteRepo.confirmAlert.submit"),
            onClick: () => deleteRepoCallback()
          },
          {
            className: "is-info",
            label: t("deleteRepo.confirmAlert.cancel"),
            onClick: () => null,
            autofocus: true
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
          <div>
            <h4 className="has-text-weight-bold">{t("deleteRepo.subtitle")}</h4>
            <p>{t("deleteRepo.description")}</p>
          </div>
        }
        right={<DeleteButton label={t("deleteRepo.button")} action={action} loading={isLoading} />}
      />
    </>
  );
};

export default DeleteRepo;
