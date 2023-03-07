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
import InputField from "./InputField";
import { useScmFormPathContext } from "../FormPathContext";
import { prefixWithoutIndices } from "../helpers";

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
            className={className}
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
        render={({ field, fieldState }) => (
          <InputField
            className={className}
            type="password"
            readOnly={readOnly ?? formReadonly}
            disabled={props.disabled}
            {...field}
            form={formId}
            label={confirmationLabelTranslation}
            helpText={confirmationHelpTextTranslation}
            error={
              fieldState.error
                ? fieldState.error.message ||
                  t(`${prefixedNameWithoutIndices}.confirmation.error.${fieldState.error.type}`)
                : undefined
            }
            testId={confirmationTestId ?? `input-${nameWithPrefix}-confirmation`}
          />
        )}
        rules={{
          validate: (value) => secretValue === value || confirmationErrorMessageTranslation,
        }}
      />
    </>
  );
}
