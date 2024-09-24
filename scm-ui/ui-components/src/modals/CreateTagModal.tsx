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
import { apiClient, Button, InputField, Modal } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Tag } from "@scm-manager/ui-types";
import { isBranchValid } from "../validation";

type Props = {
  existingTagsLink: string;
  tagCreationLink: string;
  onClose: () => void;
  onCreated: () => void;
  onError: (error: Error) => void;
  revision: string;
};

/**
 * @deprecated
 */
const CreateTagModal: FC<Props> = ({ onClose, tagCreationLink, existingTagsLink, onCreated, onError, revision }) => {
  const [t] = useTranslation("repos");
  const [newTagName, setNewTagName] = useState("");
  const [loading, setLoading] = useState(false);
  const [tagNames, setTagNames] = useState<string[]>([]);
  const initialFocusRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    apiClient
      .get(existingTagsLink)
      .then(response => response.json())
      .then(json => setTagNames(json._embedded.tags.map((tag: Tag) => tag.name)));
  }, [existingTagsLink]);

  const createTag = () => {
    setLoading(true);
    apiClient
      .post(tagCreationLink, {
        revision,
        name: newTagName
      })
      .then(onCreated)
      .catch(onError)
      .finally(() => setLoading(false));
  };

  let validationError = "";

  if (newTagName !== "") {
    if (tagNames.includes(newTagName)) {
      validationError = "tags.create.form.field.name.error.exists";
    } else if (!isBranchValid(newTagName)) {
      validationError = "tags.create.form.field.name.error.format";
    }
  }

  return (
    <Modal
      title={t("tags.create.title")}
      active={true}
      body={
        <>
          <InputField
            name="name"
            label={t("tags.create.form.field.name.label")}
            onChange={e => setNewTagName(e.target.value)}
            value={newTagName}
            validationError={!!validationError}
            errorMessage={t(validationError)}
            onReturnPressed={() => !validationError && newTagName.length > 0 && createTag()}
            ref={initialFocusRef}
          />
          <div className="mt-6">{t("tags.create.hint")}</div>
        </>
      }
      footer={
        <>
          <Button action={onClose}>{t("tags.create.cancel")}</Button>
          <Button
            color="success"
            action={() => createTag()}
            loading={loading}
            disabled={!!validationError || newTagName.length === 0}
          >
            {t("tags.create.confirm")}
          </Button>
        </>
      }
      closeFunction={onClose}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default CreateTagModal;
