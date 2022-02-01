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
