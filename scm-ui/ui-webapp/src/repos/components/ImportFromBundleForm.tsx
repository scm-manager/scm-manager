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
  disabled,
}) => {
  const [t] = useTranslation("repos");

  return (
    <>
      <div className="columns">
        <div className="column is-half is-vcentered">
          <LabelWithHelpIcon label={t("import.bundle.title")} helpText={t("import.bundle.helpText")} />
          <FileUpload
            handleFile={(file) => {
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
            onChange={(value) => setPassword(value)}
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
