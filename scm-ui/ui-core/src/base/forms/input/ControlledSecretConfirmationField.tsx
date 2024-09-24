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

import React, { ComponentProps, useCallback, useMemo } from "react";
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
  confirmationLabel?: string;
  confirmationHelpText?: string;
  confirmationErrorMessage?: string;
  confirmationTestId?: string;
};

export default function ControlledSecretConfirmationField<T extends Record<string, unknown>>({
  name,
  label,
  confirmationLabel,
  helpText,
  confirmationHelpText,
  rules,
  confirmationErrorMessage,
  className,
  testId,
  confirmationTestId,
  defaultValue,
  readOnly,
  ...props
}: Props<T>) {
  const { control, watch, t, readOnly: formReadonly, formId } = useScmFormContext();
  const formPathPrefix = useScmFormPathContext();
  const nameWithPrefix = formPathPrefix ? `${formPathPrefix}.${name}` : name;
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const labelTranslation = label || t(`${prefixedNameWithoutIndices}.label`) || "";
  const helpTextTranslation = helpText || t(`${prefixedNameWithoutIndices}.helpText`);
  const confirmationLabelTranslation = confirmationLabel || t(`${prefixedNameWithoutIndices}.confirmation.label`) || "";
  const confirmationHelpTextTranslation =
    confirmationHelpText || t(`${prefixedNameWithoutIndices}.confirmation.helpText`);
  const confirmationErrorMessageTranslation =
    confirmationErrorMessage || t(`${prefixedNameWithoutIndices}.confirmation.errorMessage`);
  const secretValue = watch(nameWithPrefix);
  const validateConfirmField = useCallback(
    (value) => secretValue === value || confirmationErrorMessageTranslation,
    [confirmationErrorMessageTranslation, secretValue]
  );
  const confirmFieldRules = useMemo(
    () => ({
      validate: validateConfirmField,
    }),
    [validateConfirmField]
  );

  return (
    <>
      <Controller
        control={control}
        name={nameWithPrefix}
        defaultValue={defaultValue as never}
        rules={{
          ...rules,
          deps: [`${nameWithPrefix}Confirmation`],
        }}
        render={({ field, fieldState }) => (
          <InputField
            className={classNames("column", className)}
            readOnly={readOnly ?? formReadonly}
            {...props}
            {...field}
            form={formId}
            required={rules?.required as boolean}
            type="password"
            label={labelTranslation}
            helpText={helpTextTranslation}
            error={
              fieldState.error
                ? fieldState.error.message || t(`${prefixedNameWithoutIndices}.error.${fieldState.error.type}`)
                : undefined
            }
            testId={testId ?? `input-${nameWithPrefix}`}
          />
        )}
      />
      <Controller
        control={control}
        name={`${nameWithPrefix}Confirmation`}
        defaultValue={defaultValue as never}
        render={({ field, fieldState: { error } }) => (
          <InputField
            className={classNames("column", className)}
            type="password"
            readOnly={readOnly ?? formReadonly}
            disabled={props.disabled}
            {...field}
            form={formId}
            label={confirmationLabelTranslation}
            helpText={confirmationHelpTextTranslation}
            error={
              error ? error.message || t(`${prefixedNameWithoutIndices}.confirmation.error.${error.type}`) : undefined
            }
            testId={confirmationTestId ?? `input-${nameWithPrefix}-confirmation`}
          />
        )}
        rules={confirmFieldRules}
      />
    </>
  );
}
