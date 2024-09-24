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
