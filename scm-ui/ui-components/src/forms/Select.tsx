/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
