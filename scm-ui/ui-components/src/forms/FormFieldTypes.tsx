import React, { ChangeEvent, FC, FocusEvent } from "react";

export type MinimumBaseProps = {
  name?: string;
};

export type LegacyProps<Base extends MinimumBaseProps, ValueType> = Base & {
  onChange?: (value: ValueType, name?: string) => void;
  onBlur?: (value: ValueType, name?: string) => void;
  innerRef?: never;
};

export type RefProps<Base extends MinimumBaseProps, ElementType extends HTMLElement> = Base & {
  onChange?: (event: ChangeEvent<ElementType>) => void;
  onBlur?: (event: FocusEvent<ElementType>) => void;
};

export type InnerRefProps<Base extends MinimumBaseProps, ElementType extends HTMLElement> = RefProps<
  Base,
  ElementType
> & {
  innerRef: React.ForwardedRef<ElementType>;
};

export const isUsingRef = <Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType>(
  props: Partial<FieldProps<Base, ElementType, ValueType>>
): props is InnerRefProps<Base, ElementType> => {
  return (props as Partial<InnerRefProps<Base, ElementType>>).innerRef !== undefined;
};

export const isLegacy = <Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType>(
  props: FieldProps<Base, ElementType, ValueType>
): props is LegacyProps<Base, ValueType> => {
  return (props as Partial<InnerRefProps<Base, ElementType>>).innerRef === undefined;
};

export type FieldProps<Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType> =
  | LegacyProps<Base, ValueType>
  | InnerRefProps<Base, ElementType>;

export type OuterProps<Base extends MinimumBaseProps, ElementType extends HTMLElement> = RefProps<Base, ElementType> & {
  ref: React.Ref<ElementType>;
};

export type FieldType<Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType> = {
  (props: OuterProps<Base, ElementType>): React.ReactElement<OuterProps<Base, ElementType>> | null;
  (props: LegacyProps<Base, ValueType>): React.ReactElement<LegacyProps<Base, ValueType>> | null;
};

export const createFormFieldWrapper = <Base extends MinimumBaseProps, ElementType extends HTMLElement, ValueType>(
  InnerType: FC<FieldProps<Base, ElementType, ValueType>>
) =>
  React.forwardRef<ElementType, LegacyProps<Base, ValueType> | OuterProps<Base, ElementType>>((props, ref) => {
    if (ref) {
      return <InnerType innerRef={ref} {...(props as RefProps<Base, ElementType>)} />;
    }
    return <InnerType {...(props as LegacyProps<Base, ValueType>)} />;
  });
