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

import React, { ComponentProps, useCallback } from "react";
import { createAttributesForTesting, useGeneratedId } from "@scm-manager/ui-components";
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

const StyledChipInput = styled(ChipInput)`
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
  &:focus {
    outline: none;
  }
`;

type InputFieldProps = {
  label: string;
  createDeleteText?: (value: string) => string;
  helpText?: string;
  error?: string;
  testId?: string;
  id?: string;
} & Pick<ComponentProps<typeof NewChipInput>, "placeholder"> &
  Pick<ComponentProps<typeof ChipInput>, "onChange" | "value" | "readOnly" | "disabled"> &
  Pick<ComponentProps<typeof Field>, "className">;

/**
 * @beta
 * @since 2.44.0
 */
const ChipInputField = React.forwardRef<HTMLInputElement, InputFieldProps>(
  (
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
      ...props
    },
    ref
  ) => {
    const [t] = useTranslation("commons", { keyPrefix: "form.chipList" });
    const deleteTextCallback = useCallback(
      (item) => (createDeleteText ? createDeleteText(item) : t("delete", { item })),
      [createDeleteText, t]
    );
    const inputId = useGeneratedId(id ?? testId);
    const labelId = useGeneratedId();
    const inputDescriptionId = useGeneratedId();
    const variant = error ? "danger" : undefined;
    return (
      <Field className={className} aria-owns={inputId}>
        <Label id={labelId}>
          {label}
          {helpText ? <Help className="ml-1" text={helpText} /> : null}
        </Label>
        <StyledChipInput
          value={value}
          onChange={onChange}
          className="is-flex is-flex-wrap-wrap input"
          aria-labelledby={labelId}
          disabled={readOnly || disabled}
        >
          {value?.map((val, index) => (
            <ChipInput.Chip key={`${val}-${index}`} className="tag is-light">
              {val}
              <ChipInput.Chip.Delete aria-label={deleteTextCallback(val)} index={index} className="delete is-small" />
            </ChipInput.Chip>
          ))}
          <StyledInput
            {...props}
            className={classNames(
              "is-borderless",
              "is-flex-grow-1",
              "has-background-transparent",
              createVariantClass(variant)
            )}
            placeholder={!readOnly && !disabled ? placeholder : ""}
            id={inputId}
            ref={ref}
            aria-describedby={inputDescriptionId}
            {...createAttributesForTesting(testId)}
          />
        </StyledChipInput>
        <VisuallyHidden aria-hidden id={inputDescriptionId}>
          {t("input.description")}
        </VisuallyHidden>
        {error ? <FieldMessage variant={variant}>{error}</FieldMessage> : null}
      </Field>
    );
  }
);
export default ChipInputField;
