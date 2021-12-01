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
import { CUSTOM_NAMESPACE_STRATEGY, Repository } from "@scm-manager/ui-types";
import { Button, ButtonGroup, ErrorNotification, InputField, Level, Loading, Modal } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Redirect } from "react-router-dom";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import * as validator from "../components/form/repositoryValidation";
import { useNamespaceStrategies, useRenameRepository } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
};

const RenameRepository: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const [showModal, setShowModal] = useState(false);
  const [name, setName] = useState(repository.name);
  const [namespace, setNamespace] = useState(repository.namespace);
  const [nameValidationError, setNameValidationError] = useState(false);
  const [namespaceValidationError, setNamespaceValidationError] = useState(false);
  const { isLoading: isRenaming, renameRepository, isRenamed, error: renamingError } = useRenameRepository(repository);
  const {
    isLoading: isLoadingNamespaceStrategies,
    error: namespaceStrategyLoadError,
    data: namespaceStrategies
  } = useNamespaceStrategies();

  if (isRenamed) {
    return <Redirect to={`/repo/${namespace}/${name}`} />;
  }

  if (namespaceStrategyLoadError) {
    return <ErrorNotification error={namespaceStrategyLoadError} />;
  }

  if (isLoadingNamespaceStrategies) {
    return <Loading />;
  }

  const isValid =
    !nameValidationError &&
    !namespaceValidationError &&
    (repository.name !== name || repository.namespace !== namespace);

  const handleNamespaceChange = (namespace: string) => {
    setNamespaceValidationError(!validator.isNamespaceValid(namespace));
    setNamespace(namespace);
  };

  const handleNameChange = (name: string) => {
    setNameValidationError(!validator.isNameValid(name));
    setName(name);
  };

  const renderNamespaceField = () => {
    const props = {
      label: t("repository.namespace"),
      helpText: t("help.namespaceHelpText"),
      value: namespace,
      onChange: handleNamespaceChange,
      errorMessage: t("validation.namespace-invalid"),
      validationError: namespaceValidationError
    };

    if (namespaceStrategies!.current === CUSTOM_NAMESPACE_STRATEGY) {
      return <InputField {...props} />;
    }

    return <ExtensionPoint name="repos.create.namespace" props={props} renderAll={false} />;
  };

  const modalBody = (
    <div>
      {renamingError ? <ErrorNotification error={renamingError} /> : null}
      <InputField
        label={t("renameRepo.modal.label.repoName")}
        name={t("renameRepo.modal.label.repoName")}
        errorMessage={t("validation.name-invalid")}
        helpText={t("help.nameHelpText")}
        validationError={nameValidationError}
        value={name}
        onChange={handleNameChange}
      />
      {renderNamespaceField()}
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
          loading={isRenaming}
          action={() => renameRepository(namespace, name)}
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
            <h4 className="has-text-weight-bold">{t("renameRepo.subtitle")}</h4>
            <p>
              {t("renameRepo.description1")}
              <br />
              {t("renameRepo.description2")}
            </p>
          </div>
        }
        right={<Button label={t("renameRepo.button")} action={() => setShowModal(true)} color="warning" icon="edit" />}
      />
    </>
  );
};

export default RenameRepository;
