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

import React, { FC } from "react";
import { FileInputField, InputField } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";

type Props = {
  setFile: (file: File) => void;
  setValid: (valid: boolean) => void;
  password: string;
  setPassword: (password: string) => void;
};

const ImportFullRepositoryForm: FC<Props> = ({ setFile, setValid, password, setPassword }) => {
  const [t] = useTranslation("repos");

  return (
    <div className="columns">
      <div className="column is-half is-vcentered">
        <FileInputField
          label={t("import.fullImport.title")}
          descriptionText={t("import.fullImport.helpText")}
          onChange={(event) => {
            const file = event.target.files?.[0];
            if (file) {
              setFile(file);
            }
            setValid(!!file);
          }}
          required
        />
      </div>
      <div className="column is-half is-vcentered">
        <InputField
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          type="password"
          label={t("import.bundle.password.title")}
          descriptionText={t("import.bundle.password.helpText")}
        />
      </div>
    </div>
  );
};

export default ImportFullRepositoryForm;
