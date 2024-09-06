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

import React, { ChangeEvent, FC, FocusEvent, KeyboardEvent } from "react";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import useAutofocus from "./useAutofocus";
import classNames from "classnames";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";
import { createA11yId } from "../createA11yId";

type BaseProps = {
  name?: string;
  label?: string;
  placeholder?: string;
  value?: string;
  autofocus?: boolean;
  helpText?: string;
  disabled?: boolean;
  onSubmit?: () => void;
  onCancel?: () => void;
  validationError?: boolean;
  errorMessage?: string | string[];
  informationMessage?: string;
  defaultValue?: string;
  readOnly?: boolean;
};

/**
 * @deprecated
 */
const InnerTextarea: FC<FieldProps<BaseProps, HTMLTextAreaElement, string>> = ({
  placeholder,
  value,
  autofocus,
  name,
  label,
  helpText,
  disabled,
  onSubmit,
  onCancel,
  errorMessage,
  validationError,
  informationMessage,
  defaultValue,
  readOnly,
  ...props
}) => {
  const ref = useAutofocus<HTMLTextAreaElement>(autofocus, props.innerRef);

  const handleChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    if (props.onChange) {
      if (isUsingRef<BaseProps, HTMLTextAreaElement, string>(props)) {
        props.onChange(event);
      } else if (isLegacy(props)) {
        props.onChange(event.target.value, name);
      }
    }
  };

  const handleBlur = (event: FocusEvent<HTMLTextAreaElement>) => {
    if (props.onBlur) {
      if (isUsingRef<BaseProps, HTMLTextAreaElement, string>(props)) {
        props.onBlur(event);
      } else if (isLegacy(props)) {
        props.onBlur(event.target.value, name);
      }
    }
  };

  const onKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (onCancel && event.key === "Escape") {
      onCancel();
      return;
    }

    if (onSubmit && event.key === "Enter" && (event.ctrlKey || event.metaKey)) {
      onSubmit();
    }
  };

  const errorView = validationError ? "is-danger" : "";
  let helper;
  if (validationError) {
    helper = <p className="help is-danger">{errorMessage}</p>;
  } else if (informationMessage) {
    helper = <p className="help is-info">{informationMessage}</p>;
  }

  const id = createA11yId("textarea");
  const helpId = createA11yId("textarea");

  return (
    <fieldset className="field" disabled={readOnly}>
      <LabelWithHelpIcon label={label} helpText={helpText} id={id} helpId={helpId} />
      <div className="control">
        <textarea
          className={classNames("textarea", errorView)}
          ref={ref}
          name={name}
          placeholder={placeholder}
          onChange={handleChange}
          onBlur={handleBlur}
          value={value}
          disabled={disabled}
          onKeyDown={onKeyDown}
          defaultValue={defaultValue}
          aria-labelledby={id}
          aria-describedby={helpId}
        />
      </div>
      {helper}
    </fieldset>
  );
};

/**
 * @deprecated
 */
const Textarea: FieldType<BaseProps, HTMLTextAreaElement, string> = createFormFieldWrapper(InnerTextarea);

export default Textarea;
