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
import { CUSTOM_NAMESPACE_STRATEGY, RepositoryCreation } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { InputField } from "@scm-manager/ui-components";
import * as validator from "./form/repositoryValidation";
import { useNamespaceStrategies } from "@scm-manager/ui-api";
import NamespaceInput from "./NamespaceInput";

type Props = {
  repository: RepositoryCreation;
  onChange: (repository: RepositoryCreation) => void;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

const useNamespaceStrategy = () => {
  const { data } = useNamespaceStrategies();
  if (data) {
    return data.current;
  }
  return "";
};

const NamespaceAndNameFields: FC<Props> = ({ repository, onChange, setValid, disabled }) => {
  const namespaceStrategy = useNamespaceStrategy();
  const [nameValidationError, setNameValidationError] = useState(false);
  const [namespaceValidationError, setNamespaceValidationError] = useState(false);
  const [t] = useTranslation("repos");

  useEffect(() => {
    if (repository.name) {
      const nameValid = validator.isNameValid(repository.name);
      setNameValidationError(!nameValid);
      if (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY) {
        if (repository.namespace) {
          const namespaceValid = validator.isNamespaceValid(repository.namespace);
          setValid(!(!namespaceValid || !nameValid));
        } else {
          setValid(false);
        }
      } else {
        setValid(nameValid);
      }
    } else {
      setValid(false);
    }
  }, [repository.name, repository.namespace, namespaceStrategy, setValid]);

  const handleNamespaceChange = (namespace: string) => {
    const valid = validator.isNamespaceValid(namespace);
    setNamespaceValidationError(!valid);
    onChange({ ...repository, namespace });
  };

  const handleNameChange = (name: string) => {
    const valid = validator.isNameValid(name);
    setNameValidationError(!valid);
    onChange({ ...repository, name });
  };

  // not yet loaded
  if (namespaceStrategy === "") {
    return null;
  }

  return (
    <>
      <NamespaceInput
        namespace={repository?.namespace}
        handleNamespaceChange={handleNamespaceChange}
        namespaceStrategy={namespaceStrategy}
        namespaceValidationError={namespaceValidationError}
        disabled={disabled}
      />
      <InputField
        label={t("repository.name")}
        onChange={handleNameChange}
        value={repository ? repository.name : ""}
        validationError={nameValidationError}
        errorMessage={t("validation.name-invalid")}
        helpText={t("help.nameHelpText")}
        disabled={disabled}
      />
    </>
  );
};

export default NamespaceAndNameFields;
