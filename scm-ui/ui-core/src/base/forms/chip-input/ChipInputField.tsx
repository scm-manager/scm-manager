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

import React, { KeyboardEventHandler, PropsWithRef, ReactElement, Ref, useCallback } from "react";
import { createAttributesForTesting, useAriaId } from "../../helpers";
import Field from "../base/Field";
import Label from "../base/label/Label";
import Help from "../base/help/Help";
import FieldMessage from "../base/field-message/FieldMessage";
import styled from "styled-components";
import classNames from "classnames";
import { createVariantClass, Variant } from "../variants";
import ChipInput, { NewChipInput } from "../headless-chip-input/ChipInput";
import { VisuallyHidden } from "@radix-ui/react-visually-hidden";
import { useTranslation } from "react-i18next";
import { withForwardRef } from "../helpers";
import { Option } from "@scm-manager/ui-types";

const StyledChipInput: typeof ChipInput = styled(ChipInput)`
  min-height: 40px;
  height: min-content;
  gap: 0.5rem;
  &:focus-within {
    border: 1px solid var(--scm-info-color);
    box-shadow: rgba(51, 178, 232, 0.25) 0px 0px 0px 0.125em;
  }
`;

const StyledInput = styled(NewChipInput)`
  color: var(--scm-secondary-more-color);
  font-size: 1rem;
  height: initial;
  padding: 0;
  border-radius: 0;
  &:focus {
    outline: none;
  }
` as unknown as typeof NewChipInput;

const StyledDelete = styled(ChipInput.Chip.Delete)`
  &:focus {
    outline-offset: 0;
  }
`;

type InputFieldProps<T> = {
  label: string;
  createDeleteText?: (value: string) => string;
  helpText?: string;
  information?: string;
  error?: string;
  warning?: string;
  testId?: string;
  id?: string;
  children?: ReactElement;
  placeholder?: string;
  onChange?: (newValue: Option<T>[]) => void;
  value?: Option<T>[] | null;
  onKeyDown?: KeyboardEventHandler<HTMLInputElement>;
  readOnly?: boolean;
  disabled?: boolean;
  className?: string;
  isLoading?: boolean;
  isNewItemDuplicate?: (existingItem: Option<T>, newItem: Option<T>) => boolean;
  ref?: Ref<HTMLInputElement>;
};

const determineVariant = (error: string | undefined, warning: string | undefined): Variant | undefined => {
  if (error) {
    return "danger";
  }

  if (warning) {
    return "warning";
  }
};

/**
 * @beta
 * @since 2.44.0
 */
const ChipInputField = function ChipInputField<T>(
  {
    label,
    helpText,
    information,
    readOnly,
    disabled,
    error,
    warning,
    createDeleteText,
    onChange,
    placeholder,
    value,
    className,
    testId,
    id,
    children,
    isLoading,
    isNewItemDuplicate,
    ...props
  }: PropsWithRef<InputFieldProps<T>>,
  ref: React.ForwardedRef<HTMLInputElement>
) {
  const [t] = useTranslation("commons", { keyPrefix: "form.chipList" });
  const deleteTextCallback = useCallback(
    (item) => (createDeleteText ? createDeleteText(item) : t("delete", { item })),
    [createDeleteText, t]
  );
  const inputId = useAriaId(id ?? testId);
  const labelId = useAriaId();
  const inputDescriptionId = useAriaId();
  const variant = determineVariant(error, warning);
  return (
    <Field className={className} aria-owns={inputId}>
      <Label id={labelId}>
        {label}
        {helpText ? <Help className="ml-1" text={helpText} /> : null}
      </Label>
      {information ? <p className="mb-2"> {information} </p> : null}
      <div className={classNames("control", { "is-loading": isLoading })}>
        <StyledChipInput
          value={value}
          onChange={(e) => onChange && onChange(e ?? [])}
          className="is-flex is-flex-wrap-wrap input"
          aria-labelledby={labelId}
          disabled={readOnly || disabled}
          isNewItemDuplicate={isNewItemDuplicate}
        >
          {value?.map((option, index) => (
            <ChipInput.Chip key={option.label} className="tag is-light is-overflow-hidden">
              <span className="is-ellipsis-overflow">{option.label}</span>
              <StyledDelete aria-label={deleteTextCallback(option.label)} index={index} className="delete is-small" />
            </ChipInput.Chip>
          ))}
          <StyledInput
            {...props}
            className={classNames(
              "is-borderless",
              "has-background-transparent",
              "is-shadowless",
              "input",
              "is-ellipsis-overflow",
              createVariantClass(variant)
            )}
            placeholder={!readOnly && !disabled ? placeholder : ""}
            id={inputId}
            ref={ref}
            aria-describedby={inputDescriptionId}
            {...createAttributesForTesting(testId)}
          >
            {children ? children : null}
          </StyledInput>
        </StyledChipInput>
      </div>
      <VisuallyHidden aria-hidden id={inputDescriptionId}>
        {t("input.description")}
      </VisuallyHidden>
      {error || warning ? <FieldMessage variant={variant}>{error ? error : warning}</FieldMessage> : null}
    </Field>
  );
};
export default withForwardRef(ChipInputField);
