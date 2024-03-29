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
import { Controller, ControllerRenderProps, Path, RegisterOptions } from "react-hook-form";
import { useScmFormContext } from "../ScmFormContext";
import CheckboxField from "./CheckboxField";
import { useScmFormPathContext } from "../FormPathContext";
import { prefixWithoutIndices } from "../helpers";
import classNames from "classnames";

type Props<T extends Record<string, unknown>> = Omit<
  ComponentProps<typeof CheckboxField>,
  "label" | "defaultValue" | "required" | keyof ControllerRenderProps
> & {
  name: Path<T>;
  label?: string;
  rules?: Pick<RegisterOptions, "deps">;
};

function ControlledInputField<T extends Record<string, unknown>>({
  name,
  label,
  helpText,
  rules,
  testId,
  defaultChecked,
  readOnly,
  className,
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
      defaultValue={defaultChecked as never}
      render={({ field }) => (
        <CheckboxField
          form={formId}
          readOnly={readOnly ?? formReadonly}
          checked={field.value}
          className={classNames("column", className)}
          {...props}
          {...field}
          label={labelTranslation}
          helpText={helpTextTranslation}
          testId={testId ?? `checkbox-${nameWithPrefix}`}
        />
      )}
    />
  );
}

export default ControlledInputField;
