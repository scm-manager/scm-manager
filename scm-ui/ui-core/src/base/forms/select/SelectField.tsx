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

import React from "react";
import Field from "../base/Field";
import Control from "../base/Control";
import Label from "../base/label/Label";
import FieldMessage from "../base/field-message/FieldMessage";
import Help from "../base/help/Help";
import Select from "./Select";
import { useAriaId } from "../../helpers";

type Props = {
  label: string;
  helpText?: string;
  error?: string;
} & React.ComponentProps<typeof Select>;

/**
 * @see https://bulma.io/documentation/form/select/
 * @beta
 * @since 2.44.0
 */
const SelectField = React.forwardRef<HTMLSelectElement, Props>(
  ({ label, helpText, error, className, id, ...props }, ref) => {
    const selectId = useAriaId(id ?? props.testId);
    const variant = error ? "danger" : undefined;
    return (
      <Field className={className}>
        <Label htmlFor={selectId}>
          {label}
          {helpText ? <Help className="ml-1" text={helpText} /> : null}
        </Label>
        <Control>
          <Select id={selectId} variant={variant} ref={ref} className="is-full-width" {...props}></Select>
        </Control>
        {error ? <FieldMessage variant={variant}>{error}</FieldMessage> : null}
      </Field>
    );
  }
);
export default SelectField;
