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

import React, { ChangeEvent, FC, FocusEvent, useEffect } from "react";
import classNames from "classnames";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import { createAttributesForTesting } from "../devBuild";
import useInnerRef from "./useInnerRef";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";
import { createA11yId } from "../createA11yId";

export type SelectItem = {
  value: string;
  label: string;
};

type BaseProps = {
  name?: string;
  label?: string;
  options: SelectItem[];
  value?: string;
  loading?: boolean;
  helpText?: string;
  disabled?: boolean;
  testId?: string;
  defaultValue?: string;
  readOnly?: boolean;
  className?: string;
  addValueToOptions?: boolean;
  ariaLabelledby?: string;
};

/**
 * @deprecated
 */
const InnerSelect: FC<FieldProps<BaseProps, HTMLSelectElement, string>> = ({
  value,
  defaultValue,
  name,
  label,
  helpText,
  loading,
  disabled,
  testId,
  readOnly,
  className,
  options,
  addValueToOptions,
  ariaLabelledby,
  ...props
}) => {
  const field = useInnerRef(props.innerRef);

  let opts = options;
  if (value && addValueToOptions && !options.some((o) => o.value === value)) {
    opts = [{ label: value, value }, ...options];
  }

  const handleInput = (event: ChangeEvent<HTMLSelectElement>) => {
    if (props.onChange) {
      if (isUsingRef<BaseProps, HTMLSelectElement, string>(props)) {
        props.onChange(event);
      } else if (isLegacy(props)) {
        props.onChange(event.target.value, name);
      }
    }
  };

  const handleBlur = (event: FocusEvent<HTMLSelectElement>) => {
    if (props.onBlur) {
      if (isUsingRef<BaseProps, HTMLSelectElement, string>(props)) {
        props.onBlur(event);
      } else if (isLegacy(props)) {
        props.onBlur(event.target.value, name);
      }
    }
  };

  useEffect(() => {
    // trigger change after render, if value is null to set it to the first value
    // of the given options.
    if (!value && field.current?.value) {
      if (props.onChange) {
        if (isUsingRef<BaseProps, HTMLSelectElement, string>(props)) {
          const event = new Event("change");
          field.current?.dispatchEvent(event);
        } else if (isLegacy(props)) {
          props.onChange(field.current?.value, name);
        }
      }
    }
  }, [field, value, name]);

  const loadingClass = loading ? "is-loading" : "";
  const a11yId = ariaLabelledby || createA11yId("select");
  const helpId = createA11yId("select");

  return (
    <fieldset className="field" disabled={readOnly}>
      <LabelWithHelpIcon label={label} helpText={helpText} id={a11yId} helpId={helpId} />
      <div className={classNames("control select", loadingClass, className)}>
        <select
          name={name}
          ref={field}
          value={value}
          defaultValue={defaultValue}
          onChange={handleInput}
          onBlur={handleBlur}
          disabled={disabled}
          aria-labelledby={ariaLabelledby || (label ? a11yId : undefined)}
          aria-describedby={helpText ? helpId : undefined}
          {...createAttributesForTesting(testId)}
        >
          {opts.map((opt) => {
            return (
              <option value={opt.value} key={"KEY_" + opt.value}>
                {opt.label}
              </option>
            );
          })}
        </select>
      </div>
    </fieldset>
  );
};

/**
 * @deprecated
 */
const Select: FieldType<BaseProps, HTMLSelectElement, string> = createFormFieldWrapper(InnerSelect);

export default Select;
