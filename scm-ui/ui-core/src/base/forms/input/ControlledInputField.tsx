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
import { Controller, ControllerRenderProps, Path } from "react-hook-form";
import { useScmFormContext } from "../ScmFormContext";
import InputField from "./InputField";
import { useScmFormPathContext } from "../FormPathContext";
import { prefixWithoutIndices } from "../helpers";
import classNames from "classnames";

type Props<T extends Record<string, unknown>> = Omit<
  ComponentProps<typeof InputField>,
  "error" | "label" | "defaultChecked" | "required" | keyof ControllerRenderProps
> & {
  rules?: ComponentProps<typeof Controller>["rules"];
  name: Path<T>;
  label?: string;
  icon?: string;
};

function ControlledInputField<T extends Record<string, unknown>>({
  name,
  label,
  helpText,
  descriptionText,
  rules,
  testId,
  defaultValue,
  readOnly,
  className,
  icon,
  ...props
}: Props<T>) {
  const { control, t, readOnly: formReadonly, formId } = useScmFormContext();
  const formPathPrefix = useScmFormPathContext();
  const nameWithPrefix = formPathPrefix ? `${formPathPrefix}.${name}` : name;
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const labelTranslation = label || t(`${prefixedNameWithoutIndices}.label`) || "";
  const helpTextTranslation = helpText || t(`${prefixedNameWithoutIndices}.helpText`);
  const descriptionTextTranslation = descriptionText || t(`${prefixedNameWithoutIndices}.descriptionText`);

  return (
    <Controller
      control={control}
      name={nameWithPrefix}
      rules={rules}
      defaultValue={defaultValue as never}
      render={({ field, fieldState }) => (
        <InputField
          readOnly={readOnly ?? formReadonly}
          required={rules?.required as boolean}
          className={classNames("column", className)}
          {...props}
          {...field}
          form={formId}
          label={labelTranslation}
          icon={icon}
          helpText={helpTextTranslation}
          descriptionText={descriptionTextTranslation}
          error={
            fieldState.error
              ? fieldState.error.message || t(`${prefixedNameWithoutIndices}.error.${fieldState.error.type}`)
              : undefined
          }
          testId={testId ?? `input-${nameWithPrefix}`}
        />
      )}
    />
  );
}

export default ControlledInputField;
