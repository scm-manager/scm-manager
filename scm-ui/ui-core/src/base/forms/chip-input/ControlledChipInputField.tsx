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

import React, { ComponentProps } from "react";
import { Controller, ControllerRenderProps, Path } from "react-hook-form";
import { useScmFormContext } from "../ScmFormContext";
import { useScmFormPathContext } from "../FormPathContext";
import { defaultOptionFactory, prefixWithoutIndices, withForwardRef } from "../helpers";
import classNames from "classnames";
import ChipInputField from "./ChipInputField";
import { Option } from "@scm-manager/ui-types";

type Props<T extends Record<string, unknown>> = Omit<
  Parameters<typeof ChipInputField>[0],
  "error" | "createDeleteText" | "label" | "defaultChecked" | "required" | keyof ControllerRenderProps
> & {
  rules?: ComponentProps<typeof Controller>["rules"];
  name: Path<T>;
  label?: string;
  defaultValue?: string[];
  createDeleteText?: (value: string) => string;
  optionFactory?: (val: any) => Option<unknown>;
  ref?: React.ForwardedRef<HTMLInputElement>;
};

/**
 * @beta
 * @since 2.44.0
 */
function ControlledChipInputField<T extends Record<string, unknown>>(
  {
    name,
    label,
    helpText,
    rules,
    testId,
    defaultValue,
    readOnly,
    placeholder,
    className,
    createDeleteText,
    children,
    optionFactory = defaultOptionFactory,
    ...props
  }: Props<T>,
  ref: React.ForwardedRef<HTMLInputElement>
) {
  const { control, t, readOnly: formReadonly } = useScmFormContext();
  const formPathPrefix = useScmFormPathContext();

  const nameWithPrefix = formPathPrefix ? `${formPathPrefix}.${name}` : name;
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const labelTranslation = label || t(`${prefixedNameWithoutIndices}.label`) || "";
  const placeholderTranslation = placeholder || t(`${prefixedNameWithoutIndices}.placeholder`) || "";
  const ariaLabelTranslation = t(`${prefixedNameWithoutIndices}.ariaLabel`);
  const helpTextTranslation = helpText || t(`${prefixedNameWithoutIndices}.helpText`);
  return (
    <Controller
      control={control}
      name={nameWithPrefix}
      rules={rules}
      defaultValue={defaultValue}
      render={({ field: { value, onChange, ...field }, fieldState }) => (
        <ChipInputField
          label={labelTranslation}
          helpText={helpTextTranslation}
          placeholder={placeholderTranslation}
          aria-label={ariaLabelTranslation}
          value={value ? value.map(optionFactory) : []}
          onChange={(selectedOptions) => onChange(selectedOptions.map((item) => item.value))}
          {...props}
          {...field}
          readOnly={readOnly ?? formReadonly}
          className={classNames("column", className)}
          error={
            fieldState.error
              ? fieldState.error.message || t(`${prefixedNameWithoutIndices}.error.${fieldState.error.type}`)
              : undefined
          }
          testId={testId ?? `input-${nameWithPrefix}`}
          ref={ref}
        >
          {children}
        </ChipInputField>
      )}
    />
  );
}

export default withForwardRef(ControlledChipInputField);
