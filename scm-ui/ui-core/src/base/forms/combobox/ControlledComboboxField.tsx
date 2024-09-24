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
import { useScmFormPathContext } from "../FormPathContext";
import { defaultOptionFactory, prefixWithoutIndices } from "../helpers";
import classNames from "classnames";
import ComboboxField from "./ComboboxField";
import { Option } from "@scm-manager/ui-types";

type Props<T extends Record<string, unknown>> = Omit<
  Parameters<typeof ComboboxField>[0],
  "error" | "label" | keyof ControllerRenderProps
> & {
  rules?: ComponentProps<typeof Controller>["rules"];
  name: Path<T>;
  label?: string;
  optionFactory?: (val: any) => Option<unknown>;
};

/**
 * @beta
 * @since 2.45.0
 */
function ControlledComboboxField<T extends Record<string, unknown>>({
  name,
  label,
  helpText,
  rules,
  testId,
  defaultValue,
  readOnly,
  className,
  optionFactory = defaultOptionFactory,
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
      render={({ field: { onChange, value, ...field }, fieldState }) => (
        // @ts-ignore
        <ComboboxField
          form={formId}
          readOnly={readOnly ?? formReadonly}
          className={classNames("column", className)}
          {...props}
          {...field}
          label={labelTranslation}
          helpText={helpTextTranslation}
          onChange={(e) => onChange(e?.value)}
          value={optionFactory(value)}
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

export default ControlledComboboxField;
