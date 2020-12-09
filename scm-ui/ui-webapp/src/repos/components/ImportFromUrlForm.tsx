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
import { RepositoryUrlImport } from "@scm-manager/ui-types";
import { InputField, validation } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import styled from "styled-components";

type Props = {
  repository: RepositoryUrlImport;
  onChange: (repository: RepositoryUrlImport) => void;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

const Column = styled.div`
  padding: 0 0.75rem;
`;

const Columns = styled.div`
  padding: 0.75rem 0 0;
`;

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
      const match = importUrl.match(/([^\/]+?)(?:.git)?$/);
      if (match && match[1]) {
        onChange({ ...repository, name: match[1] });
      }
    }
  };

  return (
    <Columns className="columns is-multiline">
      <Column className="column is-full">
        <InputField
          label={t("import.importUrl")}
          onChange={handleImportUrlChange}
          value={repository.importUrl}
          helpText={t("help.importUrlHelpText")}
          validationError={urlValidationError}
          errorMessage={t("validation.url-invalid")}
          disabled={disabled}
          onBlur={handleImportUrlBlur}
        />
      </Column>
      <Column className="column is-half">
        <InputField
          label={t("import.username")}
          onChange={username => onChange({ ...repository, username })}
          value={repository.username}
          helpText={t("help.usernameHelpText")}
          disabled={disabled}
        />
      </Column>
      <Column className="column is-half">
        <InputField
          label={t("import.password")}
          onChange={password => onChange({ ...repository, password })}
          value={repository.password}
          type="password"
          helpText={t("help.passwordHelpText")}
          disabled={disabled}
        />
      </Column>
    </Columns>
  );
};

export default ImportFromUrlForm;
