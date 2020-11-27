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

import React, {FC, useEffect, useState} from "react";
import { Modal, InputField, Button, apiClient } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  tagCreationLink: string;
  tagNames: string[];
  onClose: () => void;
  onCreated: () => void;
  onError: (error: Error) => void;
  revision: string;
};

const CreateTagModal: FC<Props> = ({ t, onClose, tagCreationLink, onCreated, onError, revision, tagNames }) => {
  const [newTagName, setNewTagName] = useState("");
  const [loading, setLoading] = useState(false);

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

  const isInvalid = () => {
    return tagNames.includes(newTagName);
  };

  return (
    <Modal
      title={t("tags.create.title")}
      active={true}
      body={
        <>
          <InputField
            name="name"
            label={t("tags.create.form.field.name.label")}
            onChange={val => setNewTagName(val)}
            value={newTagName}
            validationError={isInvalid()}
            errorMessage={t("tags.create.form.field.name.error")}
          />
          <div className="mt-5">{t("tags.create.hint")}</div>
        </>
      }
      footer={
        <>
          <Button action={onClose}>{t("tags.create.cancel")}</Button>
          <Button color="success" action={() => createTag()} loading={loading} disabled={isInvalid()}>
            {t("tags.create.confirm")}
          </Button>
        </>
      }
      closeFunction={onClose}
    />
  );
};

export default withTranslation("repos")(CreateTagModal);
