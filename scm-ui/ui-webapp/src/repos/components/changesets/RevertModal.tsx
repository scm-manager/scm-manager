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

import React, { FC, useRef, useState } from "react";
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import queryString from "query-string";
import { useBranches, useRevert } from "@scm-manager/ui-api";
import { Select, Textarea } from "@scm-manager/ui-forms";
import { Modal } from "@scm-manager/ui-components";
import { Button, ErrorNotification, Label, Loading, RequiredMarker } from "@scm-manager/ui-core";
import { Changeset, Repository } from "@scm-manager/ui-types";

type Props = {
  changeset: Changeset;
  repository: Repository;
  onClose: () => void;
};

const RevertModal: FC<Props> = ({ repository, changeset, onClose }) => {
  const [t] = useTranslation("repos");
  const history = useHistory();
  const { isLoading: isBranchesLoading, error: branchesError, data: branchData } = useBranches(repository);
  const { revert, isLoading: isRevertLoading, error: revertError } = useRevert(changeset);
  const ref = useRef<HTMLSelectElement | null>(null);
  const queryParams = queryString.parse(window.location.search);
  const [selectedBranch, setSelectedBranch] = useState<string>(getSelectedBranch(queryParams));
  const [textareaValue, setTextareaValue] = useState<string>(
    changeset?.description.length > 0
      ? t("changeset.revert.modal.commitMessagePlaceholder", {
          description: changeset.description.split("\n")[0],
          id: changeset.id,
        })
      : ""
  );

  const mappedBranches = [
    { label: "", value: "", hidden: true },
    ...(branchData?._embedded?.branches?.map((branch) => ({
      label: branch.name,
      value: branch.name,
    })) || []),
  ];

  const handleSelectChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedBranch(event.target.value);
  };

  const handleTextareaChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    setTextareaValue(event.target.value);
  };

  let body;
  if (isRevertLoading) {
    body = <Loading />;
  } else if (revertError) {
    body = <ErrorNotification error={revertError} />;
  } else if (branchesError) {
    body = <ErrorNotification error={branchesError} />;
  } else {
    body = (
      <>
        <div>
          <p className="mb-2">
            {t("changeset.revert.modal.description", {
              commit: changeset.id.substring(0, 7),
            })}
          </p>
          <Label className="is-flex is-align-items-baseline">
            <span className="mr-2">{t("changeset.revert.modal.branch")}</span>
            <Select options={mappedBranches} ref={ref} onChange={handleSelectChange} defaultValue={selectedBranch} />
          </Label>
        </div>
        <br />
        <Label>
          {t("changeset.revert.modal.commitMessage")}
          <RequiredMarker />
          <Textarea value={textareaValue} onChange={handleTextareaChange} />
        </Label>
      </>
    );
  }

  return (
    <Modal
      title={t("changeset.revert.modal.title")}
      active={true}
      body={body}
      footer={
        <>
          <Button onClick={onClose}>{t("changeset.revert.modal.cancel")}</Button>
          <Button
            variant="primary"
            onClick={() =>
              revert(
                {
                  branch: selectedBranch,
                  message: textareaValue,
                },
                {
                  onSuccess: (response) => {
                    onClose();
                    history.push(
                      `/repo/${repository.namespace}/${repository.name}/code/changeset/${response.revision}`
                    );
                  },
                }
              )
            }
            isLoading={isRevertLoading || isBranchesLoading}
            disabled={!selectedBranch || !textareaValue}
          >
            {t("changeset.revert.modal.submit")}
          </Button>
        </>
      }
      closeFunction={onClose}
      initialFocusRef={ref}
    />
  );
};

export function getSelectedBranch(queryParams: queryString.ParsedQuery) {
  return queryParams?.branch ? (queryParams.branch as string) : "";
}

export default RevertModal;
