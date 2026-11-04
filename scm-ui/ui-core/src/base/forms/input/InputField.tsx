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

import React, { FC, Ref } from "react";
import classNames from "classnames";
import Field from "../base/Field";
import Control from "../base/Control";
import Label from "../base/label/Label";
import Help from "../base/help/Help";
import FieldMessage from "../base/field-message/FieldMessage";
import Input from "./Input";
import { useAriaId } from "../../helpers";
import ExpandableText from "../base/ExpandableText";
import RequiredMarker from "../misc/RequiredMarker";

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
  /**
   * Shown below the input, if there is no error.
   *
   * @since 4.0.0
   */
  warning?: string;
  icon?: string;
  ref?: Ref<HTMLInputElement>;
} & React.ComponentProps<typeof Input>;

/**
 * @see https://bulma.io/documentation/form/input/
 */
const InputField: FC<InputFieldProps> = ({
  name,
  label,
  helpText,
  descriptionText,
  extendedText,
  error,
  warning,
  icon,
  className,
  id,
  ref,
  ...props
}) => {
  const inputId = useAriaId(id ?? props.testId);
  const descriptionId = useAriaId();
  const messageId = useAriaId();
  const variant = error ? "danger" : warning ? "warning" : undefined;
  const message = error || warning;
  return (
    <Field className={className}>
      <Label htmlFor={inputId}>
        {label}
        {props.required ? <RequiredMarker /> : null}
        {helpText ? <Help className="ml-1" text={helpText} /> : null}
      </Label>
      {extendedText && descriptionText ? (
        <ExpandableText id={descriptionId} descriptionText={descriptionText} extendedDescriptionText={extendedText} />
      ) : (
        <p className="mb-2 is-size-7" id={descriptionText ? descriptionId : undefined}>
          {descriptionText}
        </p>
      )}
      <Control className={classNames({ "has-icons-left": icon })}>
        <Input
          variant={variant}
          ref={ref}
          id={inputId}
          name={name}
          aria-describedby={classNames({ [descriptionId]: descriptionText, [messageId]: message }) || undefined}
          {...props}
        ></Input>
        {icon ? (
          <span className="icon is-small is-left">
            <i className={icon} />
          </span>
        ) : null}
      </Control>
      {message ? (
        <FieldMessage id={messageId} variant={variant}>
          {message}
        </FieldMessage>
      ) : null}
    </Field>
  );
};

export default InputField;
