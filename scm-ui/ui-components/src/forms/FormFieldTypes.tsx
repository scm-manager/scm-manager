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
import React, { ChangeEvent, FC, FocusEvent } from "react";

/**
 * @deprecated
 */
export type MinimumBaseProps = {
  name?: string;
};

/**
 * @deprecated
 */
export type LegacyProps<Base extends MinimumBaseProps, ValueType> = Base & {
  onChange?: (value: ValueType, name?: string) => void;
  onBlur?: (value: ValueType, name?: string) => void;
  innerRef?: never;
};

/**
 * @deprecated
 */
export type RefProps<Base extends MinimumBaseProps, ElementType extends HTMLElement> = Base & {
  onChange?: (event: ChangeEvent<ElementType>) => void;
  onBlur?: (event: FocusEvent<ElementType>) => void;
};

/**
 * @deprecated
 */
export type InnerRefProps<Base extends MinimumBaseProps, ElementType extends HTMLElement> = RefProps<
  Base,
  ElementType
> & {
  innerRef: React.ForwardedRef<ElementType>;
};

/**
 * @deprecated
 */
export const isUsingRef = <Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType>(
  props: Partial<FieldProps<Base, ElementType, ValueType>>
): props is InnerRefProps<Base, ElementType> => {
  return (props as Partial<InnerRefProps<Base, ElementType>>).innerRef !== undefined;
};

/**
 * @deprecated
 */
export const isLegacy = <Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType>(
  props: FieldProps<Base, ElementType, ValueType>
): props is LegacyProps<Base, ValueType> => {
  return (props as Partial<InnerRefProps<Base, ElementType>>).innerRef === undefined;
};

/**
 * @deprecated
 */
export type FieldProps<Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType> =
  | LegacyProps<Base, ValueType>
  | InnerRefProps<Base, ElementType>;

/**
 * @deprecated
 */
export type OuterProps<Base extends MinimumBaseProps, ElementType extends HTMLElement> = RefProps<Base, ElementType> & {
  ref: React.Ref<ElementType>;
};

/**
 * @deprecated
 */
export type FieldType<Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType> = {
  (props: OuterProps<Base, ElementType>): React.ReactElement<OuterProps<Base, ElementType>> | null;
  (props: LegacyProps<Base, ValueType>): React.ReactElement<LegacyProps<Base, ValueType>> | null;
};

/**
 * @deprecated
 */
export const createFormFieldWrapper = <Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType>(
  InnerType: FC<FieldProps<Base, ElementType, ValueType>>
) =>
  React.forwardRef<ElementType, LegacyProps<Base, ValueType> | OuterProps<Base, ElementType>>((props, ref) => {
    if (ref) {
      return <InnerType innerRef={ref} {...(props as RefProps<Base, ElementType>)} />;
    }
    return <InnerType {...(props as LegacyProps<Base, ValueType>)} />;
  });
