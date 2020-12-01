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
import { Repository } from "@scm-manager/ui-types";
import { CUSTOM_NAMESPACE_STRATEGY } from "../modules/repos";
import { useTranslation } from "react-i18next";
import { InputField } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import * as validator from "./form/repositoryValidation";
import { getNamespaceStrategies } from "../../admin/modules/namespaceStrategies";
import { connect } from "react-redux";

type Props = {
  repository: Repository;
  onChange: (repository: Repository) => void;
  namespaceStrategy: string;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

const NamespaceAndNameFields: FC<Props> = ({ repository, onChange, namespaceStrategy, setValid, disabled }) => {
  const [t] = useTranslation("repos");
  const [namespaceValidationError, setNamespaceValidationError] = useState(false);
  const [nameValidationError, setNameValidationError] = useState(false);

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
  }, [repository.name, repository.namespace]);

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

  const renderNamespaceField = () => {
    const props = {
      label: t("repository.namespace"),
      helpText: t("help.namespaceHelpText"),
      value: repository ? repository.namespace : "",
      onChange: handleNamespaceChange,
      errorMessage: t("validation.namespace-invalid"),
      validationError: namespaceValidationError,
      disabled: disabled
    };

    if (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY) {
      return <InputField {...props} />;
    }

    return <ExtensionPoint name="repos.create.namespace" props={props} renderAll={false} />;
  };

  return (
    <>
      {renderNamespaceField()}
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

const mapStateToProps = (state: any) => {
  const namespaceStrategy = getNamespaceStrategies(state).current;
  return {
    namespaceStrategy
  };
};

export default connect(mapStateToProps)(NamespaceAndNameFields);
