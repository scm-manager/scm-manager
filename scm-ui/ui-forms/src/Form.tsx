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

import React, { FC, useCallback, useEffect, useState } from "react";
import { DeepPartial, SubmitHandler, useForm, UseFormReturn } from "react-hook-form";
import { ErrorNotification, Level } from "@scm-manager/ui-components";
import { ScmFormContextProvider } from "./ScmFormContext";
import { useTranslation } from "react-i18next";
import { Button } from "@scm-manager/ui-buttons";
import FormRow from "./FormRow";
import ControlledInputField from "./input/ControlledInputField";
import ControlledCheckboxField from "./checkbox/ControlledCheckboxField";
import ControlledSecretConfirmationField from "./input/ControlledSecretConfirmationField";
import { HalRepresentation } from "@scm-manager/ui-types";

type RenderProps<T extends Record<string, unknown>> = Omit<
  UseFormReturn<T>,
  "register" | "unregister" | "handleSubmit" | "control"
>;

const SuccessNotification: FC<{ label?: string; hide: () => void }> = ({ label, hide }) => {
  if (!label) {
    return null;
  }

  return (
    <div className="notification is-success">
      <button className="delete" onClick={hide} />
      {label}
    </div>
  );
};

type Props<FormType extends Record<string, unknown>, DefaultValues extends FormType> = {
  children: ((renderProps: RenderProps<FormType>) => React.ReactNode | React.ReactNode[]) | React.ReactNode;
  translationPath: [namespace: string, prefix: string];
  onSubmit: SubmitHandler<FormType>;
  defaultValues: Omit<DefaultValues, keyof HalRepresentation>;
  readOnly?: boolean;
  submitButtonTestId?: string;
};

function Form<FormType extends Record<string, unknown>, DefaultValues extends FormType>({
  children,
  onSubmit,
  defaultValues,
  translationPath,
  readOnly,
  submitButtonTestId,
}: Props<FormType, DefaultValues>) {
  const form = useForm<FormType>({
    mode: "onChange",
    defaultValues: defaultValues as DeepPartial<FormType>,
  });
  const { formState, handleSubmit, reset } = form;
  const [ns, prefix] = translationPath;
  const { t } = useTranslation(ns, { keyPrefix: prefix });
  const { isDirty, isValid, isSubmitting, isSubmitSuccessful } = formState;
  const [error, setError] = useState<Error | null | undefined>();
  const [showSuccessNotification, setShowSuccessNotification] = useState(false);

  // See https://react-hook-form.com/api/useform/reset/
  useEffect(() => {
    if (isSubmitSuccessful) {
      setShowSuccessNotification(true);
      reset(defaultValues as never);
    }
    /* eslint-disable-next-line react-hooks/exhaustive-deps */
  }, [isSubmitSuccessful]);

  useEffect(() => {
    if (isDirty) {
      setShowSuccessNotification(false);
    }
  }, [isDirty]);

  const translateWithFallback = useCallback<typeof t>(
    (key, ...args) => {
      const translation = t(key, ...(args as any));
      if (translation === `${prefix}.${key}`) {
        return "";
      }
      return translation;
    },
    [prefix, t]
  );

  const submit = useCallback(
    async (data) => {
      setError(null);
      try {
        return await onSubmit(data);
      } catch (e) {
        if (e instanceof Error) {
          setError(e);
        } else {
          throw e;
        }
      }
    },
    [onSubmit]
  );

  return (
    <ScmFormContextProvider {...form} readOnly={isSubmitting || readOnly} t={translateWithFallback}>
      <form onSubmit={handleSubmit(submit)}>
        {showSuccessNotification ? (
          <SuccessNotification
            label={translateWithFallback("submit-success-notification")}
            hide={() => setShowSuccessNotification(false)}
          />
        ) : null}
        {typeof children === "function" ? children(form) : children}
        {error ? <ErrorNotification error={error} /> : null}
        {!readOnly ? (
          <Level
            right={
              <Button
                type="submit"
                variant="primary"
                testId={submitButtonTestId ?? "submit-button"}
                disabled={!isDirty || !isValid}
                isLoading={isSubmitting}
              >
                {t("submit")}
              </Button>
            }
          />
        ) : null}
      </form>
    </ScmFormContextProvider>
  );
}

export default Object.assign(Form, {
  Row: FormRow,
  Input: ControlledInputField,
  Checkbox: ControlledCheckboxField,
  SecretConfirmation: ControlledSecretConfirmationField,
});
