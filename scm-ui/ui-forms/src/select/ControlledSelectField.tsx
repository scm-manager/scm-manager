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
import SelectField from "./SelectField";
import { useScmFormPathContext } from "../FormPathContext";
import { prefixWithoutIndices } from "../helpers";

type Props<T extends Record<string, unknown>> = Omit<
  ComponentProps<typeof SelectField>,
  "error" | "label" | "required" | keyof ControllerRenderProps
> & {
  rules?: ComponentProps<typeof Controller>["rules"];
  name: Path<T>;
  label?: string;
};

function ControlledSelectField<T extends Record<string, unknown>>({
  name,
  label,
  helpText,
  rules,
  testId,
  defaultValue,
  readOnly,
  ...props
}: Props<T>) {
  const { control, t, readOnly: formReadonly, formId } = useScmFormContext();
  const formPathPrefix = useScmFormPathContext();
  const nameWithPrefix = formPathPrefix ? `${formPathPrefix}.${name}` : name;
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const labelTranslation = label || t(`${prefixedNameWithoutIndices}.label`) || "";
  const helpTextTranslation = helpText || t(`${prefixedNameWithoutIndices}.helpText`);
  return (
    <Controller
      control={control}
      name={nameWithPrefix}
      rules={rules}
      defaultValue={defaultValue as never}
      render={({ field, fieldState }) => (
        <SelectField
          form={formId}
          readOnly={readOnly ?? formReadonly}
          required={rules?.required as boolean}
          {...props}
          {...field}
          label={labelTranslation}
          helpText={helpTextTranslation}
          error={
            fieldState.error
              ? fieldState.error.message || t(`${prefixedNameWithoutIndices}.error.${fieldState.error.type}`)
              : undefined
          }
          testId={testId ?? `select-${nameWithPrefix}`}
        />
      )}
    />
  );
}

export default ControlledSelectField;
