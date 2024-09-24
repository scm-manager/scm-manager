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
