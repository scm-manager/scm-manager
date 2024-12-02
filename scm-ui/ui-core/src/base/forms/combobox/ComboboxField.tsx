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
import { withForwardRef } from "../helpers";
import Combobox, { ComboboxProps } from "./Combobox";
import classNames from "classnames";
import RequiredMarker from "../misc/RequiredMarker";

/**
 * @beta
 * @since 2.45.0
 */
const ComboboxField = function ComboboxField<T>(
  {
    label,
    helpText,
    error,
    className,
    isLoading,
    required,
    ...props
  }: ComboboxProps<T> & { label: string; helpText?: string; error?: string; isLoading?: boolean; required?: boolean },
  ref: React.ForwardedRef<HTMLInputElement>
) {
  const labelId = useAriaId();
  return (
    <Field className={className}>
      <Label id={labelId}>
        {label}
        {required ? <RequiredMarker /> : null}
        {helpText ? <Help className="ml-1" text={helpText} /> : null}
      </Label>
      <div className={classNames("control", { "is-loading": isLoading })}>
        <Combobox {...props} ref={ref} aria-labelledby={labelId} />
      </div>
    </Field>
  );
};
export default withForwardRef(ComboboxField);
