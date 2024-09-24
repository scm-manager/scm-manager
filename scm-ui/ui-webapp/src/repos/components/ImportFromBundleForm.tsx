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
import { Checkbox, FileUpload, InputField, LabelWithHelpIcon } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  setFile: (file: File) => void;
  setValid: (valid: boolean) => void;
  compressed: boolean;
  setCompressed: (compressed: boolean) => void;
  password: string;
  setPassword: (password: string) => void;
  disabled: boolean;
};

const ImportFromBundleForm: FC<Props> = ({
  setFile,
  setValid,
  compressed,
  setCompressed,
  password,
  setPassword,
  disabled
}) => {
  const [t] = useTranslation("repos");

  return (
    <>
      <div className="columns">
        <div className="column is-half is-vcentered">
          <LabelWithHelpIcon label={t("import.bundle.title")} helpText={t("import.bundle.helpText")} />
          <FileUpload
            handleFile={file => {
              setFile(file);
              setValid(!!file);
            }}
          />
        </div>
        <div className="column is-half is-vcentered">
          <Checkbox
            checked={compressed}
            onChange={(value, name) => setCompressed(value)}
            label={t("import.compressed.label")}
            disabled={disabled}
            helpText={t("import.compressed.helpText")}
            title={t("import.compressed.label")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column is-half is-vcentered">
          <InputField
            value={password}
            onChange={value => setPassword(value)}
            type="password"
            label={t("import.bundle.password.title")}
            helpText={t("import.bundle.password.helpText")}
          />
        </div>
      </div>
    </>
  );
};

export default ImportFromBundleForm;
