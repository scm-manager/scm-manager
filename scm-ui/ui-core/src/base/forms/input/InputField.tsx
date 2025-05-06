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
import classNames from "classnames";
import Field from "../base/Field";
import Control from "../base/Control";
import Label from "../base/label/Label";
import FieldMessage from "../base/field-message/FieldMessage";
import Input from "./Input";
import Help from "../base/help/Help";
import { useAriaId } from "../../helpers";

export type InputFieldProps = {
  label: string;
  helpText?: string;
  descriptionText?: string;
  error?: string;
  icon?: string;
} & React.ComponentProps<typeof Input>;

/**
 * @see https://bulma.io/documentation/form/input/
 */
const InputField = React.forwardRef<HTMLInputElement, InputFieldProps>(
  ({ name, label, helpText, descriptionText, error, icon, className, id, ...props }, ref) => {
    const inputId = useAriaId(id ?? props.testId);
    const descriptionId = descriptionText ? `input-description-${name}` : undefined;
    const variant = error ? "danger" : undefined;
    return (
      <Field className={className}>
        <Label htmlFor={inputId}>
          {label}
          {helpText ? <Help className="ml-1" text={helpText} /> : null}
        </Label>
        {descriptionText ? (
          <p className="mb-2" id={descriptionId}>
            {descriptionText}
          </p>
        ) : null}
        <Control className={classNames({ "has-icons-left": icon })}>
          <Input variant={variant} ref={ref} id={inputId} aria-describedby={descriptionId} {...props}></Input>
          {icon ? (
            <span className="icon is-small is-left">
              <i className={icon} />
            </span>
          ) : null}
        </Control>
        {error ? <FieldMessage variant={variant}>{error}</FieldMessage> : null}
      </Field>
    );
  }
);
export default InputField;
