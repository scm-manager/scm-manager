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
import React, { ChangeEvent, FC, FocusEvent, useState } from "react";
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
  ref?: React.Ref<HTMLInputElement>;
};

const FileInput: FC<Props> = ({
  name,
  filenamePlaceholder,
  testId,
  helpText,
  placeholder,
  disabled,
  label,
  className,
  ref,
  onBlur,
  onChange,
}) => {
  const [t] = useTranslation("commons");
  const [file, setFile] = useState<File | null>(null);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const uploadedFile = event?.target.files?.[0];
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
};

export default FileInput;
