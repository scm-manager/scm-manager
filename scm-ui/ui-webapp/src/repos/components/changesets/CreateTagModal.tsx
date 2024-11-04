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

import React, { FC, useEffect, useRef, useState } from "react";
import { Modal, InputField, Button, Loading, ErrorNotification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Changeset, Repository } from "@scm-manager/ui-types";
import { useCreateTag, useTags } from "@scm-manager/ui-api";
import { validation } from "@scm-manager/ui-components/";

type Props = {
  changeset: Changeset;
  repository: Repository;
  onClose: () => void;
};

const CreateTagModal: FC<Props> = ({ repository, changeset, onClose }) => {
  const { isLoading, error, data: tags } = useTags(repository);
  const {
    isLoading: isLoadingCreate,
    error: errorCreate,
    create,
    tag: createdTag,
  } = useCreateTag(repository, changeset);
  const [t] = useTranslation("repos");
  const [newTagName, setNewTagName] = useState("");
  const initialFocusRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (createdTag) {
      onClose();
    }
  }, [createdTag, onClose]);

  const tagNames = tags?._embedded?.tags.map((tag) => tag.name);

  let validationError = "";

  if (tagNames !== undefined && newTagName !== "") {
    if (!isLoadingCreate && tagNames.includes(newTagName)) {
      validationError = "tags.create.form.field.name.error.exists";
    } else if (!validation.isBranchValid(newTagName)) {
      validationError = "tags.create.form.field.name.error.format";
    }
  }

  let body;
  if (isLoading) {
    body = <Loading />;
  } else if (error) {
    body = <ErrorNotification error={error} />;
  } else if (errorCreate) {
    body = <ErrorNotification error={errorCreate} />;
  } else {
    body = (
      <>
        <InputField
          name="name"
          label={t("tags.create.form.field.name.label")}
          onChange={(event) => setNewTagName(event.target.value)}
          value={newTagName}
          validationError={!!validationError}
          errorMessage={t(validationError)}
          ref={initialFocusRef}
          onReturnPressed={() => !validationError && newTagName.length > 0 && create(newTagName)}
        />
        <div className="mt-5">{t("tags.create.hint")}</div>
      </>
    );
  }

  return (
    <Modal
      title={t("tags.create.title")}
      active={true}
      body={body}
      footer={
        <>
          <Button
            color="primary"
            action={() => create(newTagName)}
            loading={isLoadingCreate}
            disabled={isLoading || isLoadingCreate || !!validationError || newTagName.length === 0}
          >
            {t("tags.create.confirm")}
          </Button>
          <Button action={onClose}>{t("tags.create.cancel")}</Button>
        </>
      }
      closeFunction={onClose}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default CreateTagModal;
