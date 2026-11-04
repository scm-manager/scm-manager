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

import Field from "../base/Field";
import Label from "../base/label/Label";
import Help from "../base/help/Help";
import React from "react";
import { useAriaId } from "../../helpers";
import Combobox, { ComboboxProps } from "./Combobox";
import classNames from "classnames";
import RequiredMarker from "../misc/RequiredMarker";
import FieldMessage from "../base/field-message/FieldMessage";
import { createVariantClass } from "../variants";

/**
 * @beta
 * @since 2.45.0
 */
const ComboboxField = function ComboboxField<T>({
  label,
  helpText,
  descriptionText,
  error,
  className,
  isLoading,
  required,
  ref,
  ...props
}: ComboboxProps<T> & {
  label: string;
  /**
   * @deprecated This property is deprecated and will be removed in future versions.
   * Use `descriptionText` instead.
   */
  helpText?: string;
  /**
   * @since 4.0.0
   */
  descriptionText?: string;
  error?: string;
  isLoading?: boolean;
  required?: boolean;
}) {
  const labelId = useAriaId();
  const descriptionId = useAriaId();
  const errorId = useAriaId();
  return (
    <Field className={className}>
      <Label id={labelId}>
        {label}
        {required ? <RequiredMarker /> : null}
        {helpText ? <Help className="ml-1" text={helpText} /> : null}
      </Label>
      {descriptionText ? (
        <p className="mb-2 is-size-7" id={descriptionId}>
          {descriptionText}
        </p>
      ) : null}
      <div className={classNames("control", { "is-loading": isLoading })}>
        {/* @ts-ignore */}
        <Combobox
          {...props}
          ref={ref}
          className={createVariantClass(error ? "danger" : undefined)}
          aria-labelledby={labelId}
          aria-describedby={
            classNames(props["aria-describedby"], { [descriptionId]: descriptionText, [errorId]: error }) || undefined
          }
        />
      </div>
      {error ? (
        <FieldMessage id={errorId} variant="danger">
          {error}
        </FieldMessage>
      ) : null}
    </Field>
  );
};
export default ComboboxField;
