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

import React, { KeyboardEventHandler, PropsWithRef, ReactElement, Ref, useCallback } from "react";
import { createAttributesForTesting, useAriaId } from "../../helpers";
import Field from "../base/Field";
import Label from "../base/label/Label";
import Help from "../base/help/Help";
import FieldMessage from "../base/field-message/FieldMessage";
import styled from "styled-components";
import classNames from "classnames";
import { createVariantClass } from "../variants";
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
  error?: string;
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

/**
 * @beta
 * @since 2.44.0
 */
const ChipInputField = function ChipInputField<T>(
  {
    label,
    helpText,
    readOnly,
    disabled,
    error,
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
  const variant = error ? "danger" : undefined;
  return (
    <Field className={className} aria-owns={inputId}>
      <Label id={labelId}>
        {label}
        {helpText ? <Help className="ml-1" text={helpText} /> : null}
      </Label>
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
      {error ? <FieldMessage variant={variant}>{error}</FieldMessage> : null}
    </Field>
  );
};
export default withForwardRef(ChipInputField);
