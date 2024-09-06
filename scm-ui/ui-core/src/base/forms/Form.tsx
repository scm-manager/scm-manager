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

import React, { FC, useCallback, useEffect, useState } from "react";
import { DeepPartial, SubmitHandler, useForm, UseFormReturn } from "react-hook-form";
import { ErrorNotification } from "../notifications";
import { Level } from  "../misc";
import { ScmFormContextProvider } from "./ScmFormContext";
import { useTranslation } from "react-i18next";
import { Button } from "../buttons";
import styled from "styled-components";
import { setValues } from "./helpers";

type RenderProps<T extends Record<string, unknown>> = Omit<
  UseFormReturn<T>,
  "register" | "unregister" | "handleSubmit" | "control"
>;

const ButtonsContainer = styled.div`
  display: flex;
  gap: 0.75rem;
`;

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
  defaultValues: DefaultValues;
  readOnly?: boolean;
  submitButtonTestId?: string;
  /**
   * Renders a button which resets the form to its default.
   * This reflects the default browser behavior for a form *reset*.
   *
   * @since 2.43.0
   */
  withDiscardChanges?: boolean;
  /**
   * Renders a button which acts as if a user manually updated all fields to supplied values.
   * The default use-case for this is to clear forms and this is also how the button is labelled.
   * You can also use it to reset the form to an original state, but it is then advised to change the button label
   * to *Reset to Defaults* by defining the *reset* translation in the form's translation object's root.
   *
   * > *Important Note:* This mechanism cannot be used to change the number of items in lists,
   * > neither on the root level nor nested.
   * > It is therefore advised not to use this property when lists or nested forms are involved.
   *
   * @since 2.43.0
   */
  withResetTo?: DefaultValues;
  /**
   * Message to display after a successful submit if no translation key is defined.
   *
   * If this is not supplied and the root level `submit-success-notification` translation key is not set,
   * no message is displayed at all.
   *
   * @since 2.43.0
   */
  successMessageFallback?: string;
};

/**
 * @beta
 * @since 2.41.0
 */
function Form<FormType extends Record<string, unknown>, DefaultValues extends FormType = FormType>({
  children,
  onSubmit,
  defaultValues,
  translationPath,
  readOnly,
  withResetTo,
  withDiscardChanges,
  successMessageFallback,
  submitButtonTestId,
}: Props<FormType, DefaultValues>) {
  const form = useForm<FormType>({
    mode: "onChange",
    defaultValues: defaultValues as DeepPartial<FormType>,
  });
  const { formState, handleSubmit, reset, setValue } = form;
  const [ns, prefix] = translationPath;
  const { t } = useTranslation(ns, { keyPrefix: prefix });
  const [defaultTranslate] = useTranslation("commons", { keyPrefix: "form" });
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
  const { isDirty, isValid, isSubmitting, isSubmitSuccessful } = formState;
  const [error, setError] = useState<Error | null | undefined>();
  const [showSuccessNotification, setShowSuccessNotification] = useState(false);
  const submitButtonLabel = t("submit", { defaultValue: defaultTranslate("submit") });
  const resetButtonLabel = t("reset", { defaultValue: defaultTranslate("reset") });
  const discardChangesButtonLabel = t("discardChanges", { defaultValue: defaultTranslate("discardChanges") });
  const successNotification = translateWithFallback("submit-success-notification", {
    defaultValue: successMessageFallback,
  });
  const overwriteValues = useCallback(() => {
    if (withResetTo) {
      setValues(withResetTo, setValue);
    }
  }, [setValue, withResetTo]);

  // See https://react-hook-form.com/api/useform/reset/
  useEffect(() => {
    if (isSubmitSuccessful) {
      setShowSuccessNotification(true);
    }
  }, [isSubmitSuccessful]);

  useEffect(() => reset(defaultValues as never), [defaultValues, reset]);

  useEffect(() => {
    if (isDirty) {
      setShowSuccessNotification(false);
    }
  }, [isDirty]);

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
    <ScmFormContextProvider {...form} readOnly={isSubmitting || readOnly} t={translateWithFallback} formId={prefix}>
      <form onSubmit={handleSubmit(submit)} onReset={() => reset()} id={prefix} noValidate></form>
      {showSuccessNotification ? (
        <SuccessNotification label={successNotification} hide={() => setShowSuccessNotification(false)} />
      ) : null}
      {typeof children === "function" ? children(form) : children}
      {error ? <ErrorNotification error={error} /> : null}
      {!readOnly ? (
        <Level
          right={
            <ButtonsContainer>
              <Button
                type="submit"
                variant="primary"
                testId={submitButtonTestId ?? "submit-button"}
                disabled={!isDirty || !isValid}
                isLoading={isSubmitting}
                form={prefix}
              >
                {submitButtonLabel}
              </Button>
              {withDiscardChanges ? (
                <Button type="reset" form={prefix} testId={`${prefix}-discard-changes-button`}>
                  {discardChangesButtonLabel}
                </Button>
              ) : null}
              {withResetTo ? (
                <Button form={prefix} onClick={overwriteValues} testId={`${prefix}-reset-button`}>
                  {resetButtonLabel}
                </Button>
              ) : null}
            </ButtonsContainer>
          }
        />
      ) : null}
    </ScmFormContextProvider>
  );
}

export default Form;
