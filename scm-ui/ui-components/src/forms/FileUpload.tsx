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

import React, { FC, useState, ChangeEvent } from "react";
import { useTranslation } from "react-i18next";

type Props = {
  handleFile: (file: File, event?: ChangeEvent<HTMLInputElement>) => void;
  filenamePlaceholder?: string;
  disabled?: boolean;
};

/**
 * @deprecated
 */
const FileUpload: FC<Props> = ({ handleFile, filenamePlaceholder = "", disabled = false }) => {
  const [t] = useTranslation("commons");
  const [file, setFile] = useState<File | null>(null);

  return (
    <div className="file is-info has-name is-fullwidth">
      <label className="file-label">
        <input
          className="file-input"
          type="file"
          name="resume"
          disabled={disabled}
          onChange={(event: ChangeEvent<HTMLInputElement>) => {
            const uploadedFile = event?.target?.files![0];
            // @ts-ignore the uploaded file doesn't match our types
            setFile(uploadedFile);
            // @ts-ignore the uploaded file doesn't match our types
            handleFile(uploadedFile, event);
          }}
        />
        <span className="file-cta">
          <span className="file-icon">
            <i className="fas fa-arrow-circle-up" />
          </span>
          <span className="file-label has-text-weight-bold">{t("fileUpload.label")}</span>
        </span>
        {file?.name ? (
          <span className="file-name">{file?.name}</span>
        ) : (
          <span className="file-name has-text-weight-light has-text-secondary">{filenamePlaceholder}</span>
        )}
      </label>
    </div>
  );
};

export default FileUpload;
