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
    if (tagNames.includes(newTagName)) {
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
