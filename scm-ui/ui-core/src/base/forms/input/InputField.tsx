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
import Help from "../base/help/Help";
import FieldMessage from "../base/field-message/FieldMessage";
import Input from "./Input";
import { useAriaId } from "../../helpers";
import ExpandableText from "../base/ExpandableText";

export type InputFieldProps = {
  label: string;
  /**
   * @deprecated This property is deprecated and will be removed in future versions.
   * Use `descriptionText` instead.
   */
  helpText?: string;
  descriptionText?: string;
  extendedText?: string;
  error?: string;
  icon?: string;
} & React.ComponentProps<typeof Input>;

/**
 * @see https://bulma.io/documentation/form/input/
 */
const InputField = React.forwardRef<HTMLInputElement, InputFieldProps>(
  ({ name, label, helpText, descriptionText, extendedText, error, icon, className, id, ...props }, ref) => {
    const inputId = useAriaId(id ?? props.testId);
    const helpTextId = helpText ? `input-helptext-${name}` : undefined;
    const descriptionId = descriptionText ? `input-description-${name}` : undefined;
    const variant = error ? "danger" : undefined;
    return (
      <Field className={className}>
        <Label htmlFor={inputId}>
          {label}
          {helpText ? <Help className="ml-1" text={helpText} /> : null}
        </Label>
        {extendedText && descriptionText ? (
          <ExpandableText id={descriptionId} descriptionText={descriptionText} extendedDescriptionText={extendedText} />
        ) : (
          <p className="mb-2" id={descriptionId}>
            {descriptionText}
          </p>
        )}
        <Control className={classNames({ "has-icons-left": icon })}>
          <Input
            variant={variant}
            ref={ref}
            id={inputId}
            aria-describedby={descriptionId || helpTextId}
            {...props}
          ></Input>
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
