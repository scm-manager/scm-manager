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

import React, { FC, useState, ChangeEvent } from "react";
import { useTranslation } from "react-i18next";
import { File } from "@scm-manager/ui-types";

type Props = {
  handleFile: (file: File) => void;
};

const FileUpload: FC<Props> = ({ handleFile }) => {
  const [t] = useTranslation("commons");
  const [file, setFile] = useState<File | null>(null);

  return (
    <div className="file is-info has-name is-fullwidth">
      <label className="file-label">
        <input
          className="file-input"
          type="file"
          name="resume"
          onChange={(event: ChangeEvent<HTMLInputElement>) => {
            const uploadedFile = event?.target?.files![0];
            // @ts-ignore
            setFile(uploadedFile);
            // @ts-ignore
            handleFile(uploadedFile);
          }}
        />
        <span className="file-cta">
          <span className="file-icon">
            <i className="fas fa-upload" />
          </span>
          <span className="file-label">{t("fileUpload.label")}</span>
        </span>
        <span className="file-name">{file?.name || ""}</span>
      </label>
    </div>
  );
};

export default FileUpload;
