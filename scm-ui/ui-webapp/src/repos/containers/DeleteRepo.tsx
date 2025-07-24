/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC, useEffect, useState } from "react";
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useDeleteRepository } from "@scm-manager/ui-api";
import { InputField } from "@scm-manager/ui-core";

type Props = {
  repository: Repository;
  confirmDialog?: boolean;
};

const DeleteRepo: FC<Props> = ({ repository, confirmDialog = true }) => {
  const history = useHistory();
  const { isLoading, error, remove, isDeleted } = useDeleteRepository({
    onSuccess: () => {
      history.push("/repos/");
    },
  });
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [enteredValue, setEnteredValue] = useState("");
  const [inputError, setInputError] = useState();
  const [errorId, setErrorId] = useState(error ? "input-error-delete" : undefined);
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
    setEnteredValue("");
    setInputError(undefined);
    setErrorId(undefined);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEnteredValue(e.target.value);
    if (isInvalidEnteredValue && !inputError) {
      setInputError(t("deleteRepo.confirmAlert.inputNotMatching"));
      setErrorId("input-error-delete");
    }
  };

  const action = confirmDialog ? confirmDelete : deleteRepoCallback;

  const confirmationText = `${repository.namespace}/${repository.name}`;
  const isInvalidEnteredValue = enteredValue !== confirmationText;

  const confirmationDialog = (
    <>
      <InputField
        name="delete"
        label={t("deleteRepo.confirmAlert.confirmationText", { confirmationText: confirmationText })}
        value={enteredValue}
        onChange={handleChange}
        error={isInvalidEnteredValue ? inputError : undefined}
        aria-describedby={errorId}
        aria-invalid={errorId !== undefined}
        autoComplete="off"
      ></InputField>
    </>
  );

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("deleteRepo.confirmAlert.title")}
        buttons={[
          {
            label: t("deleteRepo.confirmAlert.submit"),
            onClick: () => deleteRepoCallback(),
            disabled: isInvalidEnteredValue,
          },
          {
            className: "is-info",
            label: t("deleteRepo.confirmAlert.cancel"),
            onClick: () => null,
            autofocus: true,
          },
        ]}
        close={() => setShowConfirmAlert(false)}
      >
        {confirmationDialog}
      </ConfirmAlert>
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
