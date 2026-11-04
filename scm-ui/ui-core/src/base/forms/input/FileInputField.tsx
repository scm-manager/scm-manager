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

import React, { ChangeEvent, FC, InputHTMLAttributes, Ref, useState } from "react";
import classNames from "classnames";
import { useTranslation } from "react-i18next";
import Field from "../base/Field";
import Control from "../base/Control";
import Label from "../base/label/Label";
import Help from "../base/help/Help";
import FieldMessage from "../base/field-message/FieldMessage";
import RequiredMarker from "../misc/RequiredMarker";
import { createAttributesForTesting, useAriaId } from "../../helpers";

type Props = {
  label: string;
  /**
   * @deprecated This property is deprecated and will be removed in future versions.
   * Use `descriptionText` instead.
   */
  helpText?: string;
  descriptionText?: string;
  error?: string;
  /**
   * Shown instead of the file name as long as no file is selected.
   * Defaults to the translation `fileInput.noFileChosen` of the commons namespace.
   */
  filenamePlaceholder?: string;
  testId?: string;
  ref?: Ref<HTMLInputElement>;
  // a file input cannot get a value from the outside, the browser throws for any value but an empty one
} & Omit<InputHTMLAttributes<HTMLInputElement>, "type" | "value" | "defaultValue">;

/**
 * @see https://bulma.io/documentation/form/file/
 * @beta
 * @since 4.0.0
 */
const FileInputField: FC<Props> = ({
  label,
  helpText,
  descriptionText,
  error,
  filenamePlaceholder,
  className,
  id,
  testId,
  onChange,
  ref,
  ...props
}) => {
  const [t] = useTranslation("commons");
  const [fileName, setFileName] = useState<string>();
  const inputId = useAriaId(id ?? testId);
  const descriptionId = useAriaId();
  const errorId = useAriaId();

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFileName(event.target.files?.[0]?.name);
    onChange?.(event);
  };

  return (
    <Field className={className}>
      <Label htmlFor={inputId}>
        {label}
        {props.required ? <RequiredMarker /> : null}
        {helpText ? <Help className="ml-1" text={helpText} /> : null}
      </Label>
      {descriptionText ? (
        <p className="mb-2 is-size-7" id={descriptionId}>
          {descriptionText}
        </p>
      ) : null}
      <Control>
        <div className={classNames("file", "has-name", "is-fullwidth", error ? "is-danger" : "is-info")}>
          <label className="file-label">
            <input
              ref={ref}
              id={inputId}
              className="file-input"
              type="file"
              aria-describedby={classNames({ [descriptionId]: descriptionText, [errorId]: error }) || undefined}
              onChange={handleChange}
              {...props}
              {...createAttributesForTesting(testId)}
            />
            <span className="file-cta">
              <span className="file-icon">
                <i className="fas fa-arrow-circle-up" aria-hidden />
              </span>
              <span className="file-label has-text-weight-bold">{t("fileInput.label")}</span>
            </span>
            <span className={classNames("file-name", { "has-text-weight-light has-text-secondary": !fileName })}>
              {fileName ?? filenamePlaceholder ?? t("fileInput.noFileChosen")}
            </span>
          </label>
        </div>
      </Control>
      {error ? (
        <FieldMessage id={errorId} variant="danger">
          {error}
        </FieldMessage>
      ) : null}
    </Field>
  );
};

export default FileInputField;
