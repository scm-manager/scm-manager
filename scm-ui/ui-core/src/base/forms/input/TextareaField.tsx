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
import Textarea from "./Textarea";
import RequiredMarker from "../misc/RequiredMarker";
import { useAriaId } from "../../helpers";

type Props = {
  label: string;
  /**
   * @deprecated This property is deprecated and will be removed in future versions.
   * Use `descriptionText` instead.
   */
  helpText?: string;
  descriptionText?: string;
  error?: string;
  ref?: Ref<HTMLTextAreaElement>;
} & React.ComponentProps<typeof Textarea>;

/**
 * @see https://bulma.io/documentation/form/textarea/
 * @beta
 * @since 4.0.0
 */
const TextareaField: FC<Props> = ({ label, helpText, descriptionText, error, className, id, ref, ...props }) => {
  const textareaId = useAriaId(id ?? props.testId);
  const descriptionId = useAriaId();
  const errorId = useAriaId();
  const variant = error ? "danger" : undefined;
  return (
    <Field className={className}>
      <Label htmlFor={textareaId}>
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
        <Textarea
          variant={variant}
          ref={ref}
          id={textareaId}
          aria-describedby={classNames({ [descriptionId]: descriptionText, [errorId]: error }) || undefined}
          {...props}
        />
      </Control>
      {error ? (
        <FieldMessage id={errorId} variant={variant}>
          {error}
        </FieldMessage>
      ) : null}
    </Field>
  );
};

export default TextareaField;
