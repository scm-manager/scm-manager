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
import { validation } from "@scm-manager/ui-components";
import { Checkbox, Field, InputField } from "@scm-manager/ui-core";

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
          onChange={(event) => handleImportUrlChange(event.target.value)}
          value={repository.importUrl}
          descriptionText={t("help.importUrlHelpText")}
          error={urlValidationError ? t("validation.url-invalid") : undefined}
          disabled={disabled}
          onBlur={(event) => handleImportUrlBlur(event.target.value)}
          required
        />
      </div>
      <div className="column is-half px-3">
        <InputField
          label={t("import.username")}
          onChange={(event) => onChange({ ...repository, username: event.target.value })}
          value={repository.username}
          descriptionText={t("help.usernameHelpText")}
          disabled={disabled}
        />
      </div>
      <div className="column is-half px-3">
        <InputField
          label={t("import.password")}
          onChange={(event) => onChange({ ...repository, password: event.target.value })}
          value={repository.password}
          type="password"
          descriptionText={t("help.passwordHelpText")}
          disabled={disabled}
        />
      </div>
      <div className="column is-full px-3">
        <Field>
          <Checkbox
            label={t("import.skipLfs")}
            onChange={(event) => onChange({ ...repository, skipLfs: event.target.checked })}
            checked={repository.skipLfs}
            helpText={t("help.skipLfsHelpText")}
            disabled={disabled}
          />
        </Field>
      </div>
    </div>
  );
};

export default ImportFromUrlForm;
