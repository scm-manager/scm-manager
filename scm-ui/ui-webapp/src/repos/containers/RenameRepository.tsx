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
import { Repository, Link } from "@scm-manager/ui-types";
import { CONTENT_TYPE } from "../modules/repos";
import {
  ErrorNotification,
  Level,
  Button,
  Loading,
  Modal,
  InputField,
  validation,
  ButtonGroup
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { apiClient } from "@scm-manager/ui-components/src";
import { useHistory } from "react-router-dom";

type Props = {
  repository: Repository;
  renameNamespace: boolean;
};

const RenameRepository: FC<Props> = ({ repository, renameNamespace }) => {
  let history = useHistory();
  const [t] = useTranslation("repos");
  const [error, setError] = useState<Error | undefined>(undefined);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [name, setName] = useState(repository.name);
  const [namespace, setNamespace] = useState(repository.namespace);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  const isValid =
    validation.isNameValid(name) &&
    validation.isNameValid(namespace) &&
    (repository.name !== name || repository.namespace !== namespace);

  const rename = () => {
    setLoading(true);
    const url = renameNamespace
      ? (repository?._links?.renameWithNamespace as Link).href
      : (repository?._links?.rename as Link).href;

    apiClient
      .post(url, { name, namespace }, CONTENT_TYPE)
      .then(() => setLoading(false))
      .then(() => history.push(`/repo/${namespace}/${name}`))
      .catch(setError);
  };

  const modalBody = (
    <div>
      <InputField
        label={t("renameRepo.modal.label.repoName")}
        name={t("renameRepo.modal.label.repoName")}
        value={name}
        onChange={setName}
      />
      {renameNamespace && (
        <InputField
          label={t("renameRepo.modal.label.repoNamespace")}
          name={t("renameRepo.modal.label.repoNamespace")}
          value={namespace}
          onChange={setNamespace}
        />
      )}
    </div>
  );

  const footer = (
    <>
      <ButtonGroup>
        <Button
          color="warning"
          icon="exclamation-triangle"
          label={t("renameRepo.modal.button.rename")}
          disabled={!isValid}
          title={t("renameRepo.modal.button.rename")}
          action={rename}
        />
        <Button
          label={t("renameRepo.modal.button.cancel")}
          title={t("renameRepo.modal.button.cancel")}
          action={() => setShowModal(false)}
        />
      </ButtonGroup>
    </>
  );

  return (
    <>
      <Modal
        active={showModal}
        title={t("renameRepo.modal.title")}
        footer={footer}
        body={modalBody}
        closeFunction={() => setShowModal(false)}
      />
      <Level
        left={
          <div>
            <strong>{t("renameRepo.subtitle")}</strong>
            <p>{t("renameRepo.description")}</p>
          </div>
        }
        right={
          <Button
            label={t("renameRepo.button")}
            action={() => setShowModal(true)}
            loading={loading}
            color="warning"
            icon="edit"
          />
        }
      />
    </>
  );
};

export default RenameRepository;
