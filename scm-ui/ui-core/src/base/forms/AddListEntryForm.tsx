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

import React, { useCallback, useEffect } from "react";
import { ScmFormPathContextProvider, useScmFormPathContext } from "./FormPathContext";
import { ScmFormContextProvider, useScmFormContext } from "./ScmFormContext";
import { DeepPartial, UseFieldArrayReturn, useForm, UseFormReturn } from "react-hook-form";
import { Button } from "../buttons";
import { prefixWithoutIndices } from "./helpers";
import { useScmFormListContext } from "./ScmFormListContext";
import { useTranslation } from "react-i18next";
import styled from "styled-components";

const StyledHr = styled.hr`
  &:last-child {
    display: none;
  }
`;

type RenderProps<T extends Record<string, unknown>> = Omit<
  UseFormReturn<T>,
  "register" | "unregister" | "handleSubmit" | "control"
>;

type Props<FormType extends Record<string, unknown>, DefaultValues extends FormType> = {
  children: ((renderProps: RenderProps<FormType>) => React.ReactNode | React.ReactNode[]) | React.ReactNode;
  defaultValues: DefaultValues;
  submit?: (data: FormType, append: UseFieldArrayReturn["append"]) => unknown;
  /**
   * @default true
   */
  disableSubmitWhenDirty?: boolean;
};

/**
 * @beta
 * @since 2.43.0
 */
function AddListEntryForm<FormType extends Record<string, unknown>, DefaultValues extends FormType>({
  children,
  defaultValues,
  submit,
  disableSubmitWhenDirty = true,
}: Props<FormType, DefaultValues>) {
  const [defaultTranslate] = useTranslation("commons", { keyPrefix: "form" });
  const { readOnly, t } = useScmFormContext();
  const nameWithPrefix = useScmFormPathContext();
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const { append, isNested } = useScmFormListContext();
  const form = useForm<FormType, DefaultValues>({
    mode: "onChange",
    defaultValues: defaultValues as DeepPartial<FormType>,
  });

  const {
    reset,
    formState: { isSubmitSuccessful, isDirty, isValid },
  } = form;

  const translateWithExtraPrefix = useCallback<typeof t>(
    (key, ...args) => t(`${prefixedNameWithoutIndices}.${key}`, ...(args as any)),
    [prefixedNameWithoutIndices, t]
  );

  const onSubmit = useCallback((data) => (submit ? submit(data, append) : append(data)), [append, submit]);

  const submitButtonLabel = translateWithExtraPrefix("add", {
    defaultValue: defaultTranslate("list.add.label", { entity: translateWithExtraPrefix("entity") }),
  });
  const titleLabel = translateWithExtraPrefix("title", {
    defaultValue: defaultTranslate("list.title.label", { entity: translateWithExtraPrefix("entity") }),
  });

  useEffect(() => {
    if (isSubmitSuccessful) {
      reset(defaultValues);
    }
  }, [defaultValues, isSubmitSuccessful, reset]);

  if (readOnly) {
    return null;
  }

  return (
    <ScmFormContextProvider {...form} t={translateWithExtraPrefix} formId={nameWithPrefix}>
      <ScmFormPathContextProvider path="">
        <h3 className="subtitle is-5">{titleLabel}</h3>
        <form id={nameWithPrefix} onSubmit={form.handleSubmit(onSubmit)} noValidate></form>
        {typeof children === "function" ? children(form) : children}
        <div className="level-left">
          <Button
            form={nameWithPrefix}
            type="submit"
            variant={isNested ? undefined : "secondary"}
            disabled={(disableSubmitWhenDirty && !isDirty) || !isValid}
          >
            {submitButtonLabel}
          </Button>
        </div>
        <StyledHr />
      </ScmFormPathContextProvider>
    </ScmFormContextProvider>
  );
}

export default AddListEntryForm;
