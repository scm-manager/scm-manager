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
