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
import { useTranslation } from "react-i18next";
import { RepositoryUrlImport } from "@scm-manager/ui-types";
import { Checkbox, InputField, validation } from "@scm-manager/ui-components";

type Props = {
  repository: RepositoryUrlImport;
  onChange: (repository: RepositoryUrlImport) => void;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

const ImportFromUrlForm: FC<Props> = ({ repository, onChange, setValid, disabled }) => {
  const [t] = useTranslation("repos");
  const [urlValidationError, setUrlValidationError] = useState(false);

  const handleImportUrlChange = (importUrl: string) => {
    onChange({ ...repository, importUrl });
    const valid = validation.isUrlValid(importUrl);
    setUrlValidationError(!valid);
    setValid(valid);
  };

  const handleImportUrlBlur = (importUrl: string) => {
    if (!repository.name) {
      // If the repository name is not fill we set a name suggestion
      const match = importUrl.match(/([^/]+?)(?:.git)?$/);
      if (match && match[1]) {
        onChange({ ...repository, name: match[1] });
      }
    }
  };

  return (
    <div className="columns is-multiline pt-3">
      <div className="column is-full px-3">
        <InputField
          label={t("import.importUrl")}
          onChange={handleImportUrlChange}
          value={repository.importUrl}
          helpText={t("help.importUrlHelpText")}
          validationError={urlValidationError}
          errorMessage={t("validation.url-invalid")}
          disabled={disabled}
          onBlur={handleImportUrlBlur}
          required={true}
          aria-required={true}
        />
      </div>
      <div className="column is-half px-3">
        <InputField
          label={t("import.username")}
          onChange={(username) => onChange({ ...repository, username })}
          value={repository.username}
          helpText={t("help.usernameHelpText")}
          disabled={disabled}
        />
      </div>
      <div className="column is-half px-3">
        <InputField
          label={t("import.password")}
          onChange={(password) => onChange({ ...repository, password })}
          value={repository.password}
          type="password"
          helpText={t("help.passwordHelpText")}
          disabled={disabled}
        />
      </div>
      <div className="column is-full px-3">
        <Checkbox
          label={t("import.skipLfs")}
          onChange={(skipLfs) => onChange({ ...repository, skipLfs })}
          checked={repository.skipLfs}
          helpText={t("help.skipLfsHelpText")}
          disabled={disabled}
        />
      </div>
    </div>
  );
};

export default ImportFromUrlForm;
