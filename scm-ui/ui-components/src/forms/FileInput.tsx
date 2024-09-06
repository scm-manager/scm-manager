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

import React, { ChangeEvent, FocusEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { createAttributesForTesting } from "../devBuild";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import { createA11yId } from "../createA11yId";

type Props = {
  name?: string;
  filenamePlaceholder?: string;
  className?: string;
  label?: string;
  placeholder?: string;
  helpText?: string;
  disabled?: boolean;
  testId?: string;
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void;
  onBlur?: (event: FocusEvent<HTMLInputElement>) => void;
};

/**
 * @deprecated
 */
const FileInput = React.forwardRef<HTMLInputElement, Props>(
  ({ name, filenamePlaceholder, testId, helpText, placeholder, disabled, label, className, onBlur, onChange }, ref) => {
    const [t] = useTranslation("commons");
    const [file, setFile] = useState<File | null>(null);

    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
      const uploadedFile = event?.target?.files![0];
      // @ts-ignore the uploaded file doesn't match our types
      setFile(uploadedFile);

      if (onChange && event.target.files) {
        onChange(event);
      }
    };

    const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
      if (onBlur && event.target.files) {
        onBlur(event);
      }
    };

    const id = createA11yId("file-input");

    return (
      <div className={classNames("field", className)}>
        <LabelWithHelpIcon label={label} helpText={helpText} id={id} />
        <div className="file is-info has-name is-fullwidth">
          <label className="file-label">
            <input
              ref={ref}
              name={name}
              className="file-input"
              type="file"
              placeholder={placeholder}
              disabled={disabled}
              onChange={handleChange}
              onBlur={handleBlur}
              aria-describedby={id}
              {...createAttributesForTesting(testId)}
            />
            <span className="file-cta">
              <span className="file-icon">
                <i className="fas fa-arrow-circle-up" />
              </span>
              <span className="file-label has-text-weight-bold">{t("fileInput.label")}</span>
            </span>
            {file?.name ? (
              <span className="file-name">{file?.name}</span>
            ) : (
              <span className="file-name has-text-weight-light has-text-secondary">
                {filenamePlaceholder || t("fileInput.noFileChosen")}
              </span>
            )}
          </label>
        </div>
      </div>
    );
  }
);

export default FileInput;
