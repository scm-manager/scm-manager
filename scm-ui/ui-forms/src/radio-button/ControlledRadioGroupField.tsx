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
import RadioGroupField from "./RadioGroupField";
import { Controller, ControllerRenderProps, Path } from "react-hook-form";
import { useScmFormContext } from "../ScmFormContext";
import { useScmFormPathContext } from "../FormPathContext";
import { prefixWithoutIndices } from "../helpers";
import classNames from "classnames";
import { RadioButtonContextProvider } from "./RadioButtonContext";

type Props<T extends Record<string, unknown>> = Omit<
  ComponentProps<typeof RadioGroupField>,
  "label" | keyof ControllerRenderProps
> & {
  name: Path<T>;
  rules?: ComponentProps<typeof Controller>["rules"];
  label?: string;
  readOnly?: boolean;
};

/**
 * @beta
 * @since 2.48.0
 */
function ControlledRadioGroupField<T extends Record<string, unknown>>({
  name,
  label,
  helpText,
  rules,
  defaultValue,
  children,
  fieldClassName,
  readOnly,
  ...props
}: Props<T>) {
  const { control, t, readOnly: formReadOnly, formId } = useScmFormContext();
  const formPathPrefix = useScmFormPathContext();
  const nameWithPrefix = formPathPrefix ? `${formPathPrefix}.${name}` : name;
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const labelTranslation = label ?? t(`${prefixedNameWithoutIndices}.label`) ?? "";
  const helpTextTranslation = helpText ?? t(`${prefixedNameWithoutIndices}.helpText`);

  return (
    <Controller
      control={control}
      name={nameWithPrefix}
      rules={rules}
      defaultValue={defaultValue}
      render={({ field }) => (
        <RadioButtonContextProvider t={t} prefix={prefixedNameWithoutIndices} formId={formId}>
          <RadioGroupField
            defaultValue={defaultValue}
            disabled={readOnly ?? formReadOnly}
            required={rules?.required as boolean}
            label={labelTranslation}
            helpText={helpTextTranslation}
            fieldClassName={classNames("column", fieldClassName)}
            onValueChange={field.onChange}
            {...props}
            {...field}
            name={nameWithPrefix}
          >
            {children}
          </RadioGroupField>
        </RadioButtonContextProvider>
      )}
    />
  );
}

export default ControlledRadioGroupField;
