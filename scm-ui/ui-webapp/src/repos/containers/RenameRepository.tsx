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
import { Repository } from "@scm-manager/ui-types";
import { Button, ButtonGroup, ErrorNotification, InputField, Level, Loading, Modal } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Redirect } from "react-router-dom";
import * as validator from "../components/form/repositoryValidation";
import { useNamespaceStrategies, useRenameRepository } from "@scm-manager/ui-api";
import NamespaceInput from "../components/NamespaceInput";
import styled from "styled-components";

const WithOverflow = styled.div`
  overflow: visible;
`;

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
  const initialFocusRef = useRef<HTMLInputElement>(null);

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

  const modalBody = (
    <WithOverflow>
      {renamingError ? <ErrorNotification error={renamingError} /> : null}
      <InputField
        label={t("renameRepo.modal.label.repoName")}
        name={t("renameRepo.modal.label.repoName")}
        errorMessage={t("validation.name-invalid")}
        helpText={t("help.nameHelpText")}
        validationError={nameValidationError}
        value={name}
        onChange={event => handleNameChange(event.target.value)}
        onReturnPressed={() => isValid && renameRepository(namespace, name)}
        ref={initialFocusRef}
      />
      <NamespaceInput
        namespace={namespace}
        handleNamespaceChange={handleNamespaceChange}
        namespaceValidationError={namespaceValidationError}
        namespaceStrategy={namespaceStrategies?.current}
      />
    </WithOverflow>
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

  const modal = (
    <Modal
      active={showModal}
      title={t("renameRepo.modal.title")}
      footer={footer}
      body={modalBody}
      closeFunction={() => setShowModal(false)}
      initialFocusRef={initialFocusRef}
      overflowVisible={true}
    />
  );

  return (
    <>
      {showModal ? modal : null}
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
