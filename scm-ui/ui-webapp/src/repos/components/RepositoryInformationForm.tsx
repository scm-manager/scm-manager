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
import { InputField, Textarea } from "@scm-manager/ui-components";
import { RepositoryCreation } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import * as validator from "./form/repositoryValidation";

type Props = {
  repository: RepositoryCreation;
  onChange: (repository: RepositoryCreation) => void;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

const RepositoryInformationForm: FC<Props> = ({ repository, onChange, disabled, setValid }) => {
  const [t] = useTranslation("repos");
  const [contactValidationError, setContactValidationError] = useState(false);

  const handleContactChange = (contact: string) => {
    const valid = validator.isContactValid(contact);
    setContactValidationError(!valid);
    setValid(valid);
    onChange({ ...repository, contact });
  };

  return (
    <>
      <InputField
        label={t("repository.contact")}
        onChange={handleContactChange}
        value={repository ? repository.contact : ""}
        validationError={contactValidationError}
        errorMessage={t("validation.contact-invalid")}
        helpText={t("help.contactHelpText")}
        disabled={disabled}
      />
      <Textarea
        label={t("repository.description")}
        onChange={(description) => onChange({ ...repository, description })}
        value={repository ? repository.description : ""}
        helpText={t("help.descriptionHelpText")}
        disabled={disabled}
      />
    </>
  );
};

export default RepositoryInformationForm;
