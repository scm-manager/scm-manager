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
