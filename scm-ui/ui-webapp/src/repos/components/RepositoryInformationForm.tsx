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
        onChange={description => onChange({ ...repository, description })}
        value={repository ? repository.description : ""}
        helpText={t("help.descriptionHelpText")}
        disabled={disabled}
      />
    </>
  );
};

export default RepositoryInformationForm;
