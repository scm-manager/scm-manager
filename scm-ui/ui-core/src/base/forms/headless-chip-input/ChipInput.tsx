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

import React, {
  ButtonHTMLAttributes,
  ComponentProps,
  ComponentType,
  Context,
  createContext,
  HTMLAttributes,
  KeyboardEventHandler,
  LiHTMLAttributes,
  ReactElement,
  RefObject,
  useCallback,
  useContext,
  useMemo,
  useRef,
} from "react";
import { Slot } from "@radix-ui/react-slot";
import { Option } from "@scm-manager/ui-types";
import { mergeRefs, withForwardRef } from "../helpers";
import { Button } from "../../buttons";

type ChipInputContextType<T> = {
  add(newValue: Option<T>): void;
  remove(index: number): void;
  inputRef: RefObject<HTMLInputElement>;
  disabled?: boolean;
  readOnly?: boolean;
};

const ChipInputContext = createContext<ChipInputContextType<never>>(null as unknown as ChipInputContextType<never>);

function getChipInputContext<T>() {
  return ChipInputContext as unknown as Context<ChipInputContextType<T>>;
}

type CustomNewChipInputProps<T> = {
  onChange: (newValue: Option<T>) => void;
  value?: Option<T> | null;
  readOnly?: boolean;
  disabled?: boolean;
  ref?: React.Ref<HTMLInputElement>;
  className?: string;
  placeholder?: string;
  id?: string;
  "aria-describedby"?: string;
};

const DefaultNewChipInput = React.forwardRef<HTMLInputElement, CustomNewChipInputProps<string>>(
  ({ onChange, value, ...props }, ref) => {
    const handleKeyDown = useCallback<KeyboardEventHandler<HTMLInputElement>>(
      (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          if (e.currentTarget.value) {
            onChange({ label: e.currentTarget.value, value: e.currentTarget.value });
          }
          return false;
        }
      },
      [onChange]
    );

    return (
      <div className="is-flex-grow-1">
        <input onKeyDown={handleKeyDown} {...props} ref={ref} />
      </div>
    );
  }
);

type ChipDeleteProps = {
  asChild?: boolean;
  index: number;
} & Omit<ButtonHTMLAttributes<HTMLButtonElement>, "type" | "onClick">;

/**
 * @beta
 * @since 2.44.0
 */
export const ChipDelete = React.forwardRef<HTMLButtonElement, ChipDeleteProps>(({ asChild, index, ...props }, ref) => {
  const { remove, disabled } = useContext(ChipInputContext);
  const Comp = asChild ? Slot : "button";

  if (disabled) {
    return null;
  }

  return <Comp {...props} ref={ref} type="button" onClick={() => remove(index)} />;
});

type NewChipInputProps = {
  children?: ReactElement | null;
  ref?: React.Ref<HTMLInputElement>;
  className?: string;
  placeholder?: string;
  id?: string;
  "aria-describedby"?: string;
};

/**
 * @beta
 * @since 2.44.0
 */
export const NewChipInput = withForwardRef(function NewChipInput<T>(
  props: NewChipInputProps,
  ref: React.ForwardedRef<HTMLInputElement>
) {
  const { add, disabled, readOnly, inputRef } = useContext(getChipInputContext<T>());

  const Comp = props.children ? Slot : DefaultNewChipInput;
  return React.createElement(Comp as unknown as ComponentType<CustomNewChipInputProps<T>>, {
    ...props,
    onChange: add,
    readOnly,
    disabled,
    value: null,
    ref: mergeRefs(ref, inputRef),
  });
});

type ChipProps = { asChild?: boolean } & LiHTMLAttributes<HTMLLIElement>;

/**
 * @beta
 * @since 2.44.0
 */
export const Chip = React.forwardRef<HTMLLIElement, ChipProps>(({ asChild, ...props }, ref) => {
  const Comp = asChild ? Slot : "li";

  return <Comp {...props} ref={ref} />;
});

type Props<T> = {
  value?: Option<T>[] | null;
  onChange?: (newValue?: Option<T>[]) => void;
  readOnly?: boolean;
  disabled?: boolean;
  isNewItemDuplicate?: (existingItem: Option<T>, newItem: Option<T>) => boolean;
} & Omit<HTMLAttributes<HTMLUListElement>, "onChange">;

/**
 * @beta
 * @since 2.44.0
 */
const ChipInput = withForwardRef(function ChipInput<T>(
  { children, value = [], disabled, readOnly, onChange, isNewItemDuplicate, ...props }: Props<T>,
  ref: React.ForwardedRef<HTMLUListElement>
) {
  const inputRef = useRef<HTMLInputElement>(null);
  const isInactive = useMemo(() => disabled || readOnly, [disabled, readOnly]);
  const add = useCallback<(newValue: Option<T>) => void>(
    (newItem) => {
      if (
        !isInactive &&
        !value?.some((item) =>
          isNewItemDuplicate
            ? isNewItemDuplicate(item, newItem)
            : item.label === newItem.label || item.value === newItem.value
        )
      ) {
        if (onChange) {
          onChange([...(value ?? []), newItem]);
        }
        if (inputRef.current) {
          inputRef.current.value = "";
        }
      }
    },
    [isInactive, isNewItemDuplicate, onChange, value]
  );
  const remove = useCallback(
    (index: number) => !isInactive && onChange && onChange(value?.filter((_, itdx) => itdx !== index)),
    [isInactive, onChange, value]
  );
  const Context = getChipInputContext<T>();
  return (
    <Context.Provider
      value={useMemo(
        () => ({
          disabled,
          readOnly,
          add,
          remove,
          inputRef,
        }),
        [add, disabled, readOnly, remove]
      )}
    >
      <ul {...props} ref={ref}>
        {children}
      </ul>
    </Context.Provider>
  );
});

const AddButton = React.forwardRef<
  HTMLButtonElement,
  Omit<ComponentProps<typeof Button>, "onClick"> & { inputRef: RefObject<HTMLInputElement | null> }
>(({ inputRef, children, ...props }, ref) => (
  <Button
    {...props}
    onClick={() => inputRef.current?.dispatchEvent(new KeyboardEvent("keydown", { key: "Enter", bubbles: true }))}
  >
    {children}
  </Button>
));

export default Object.assign(ChipInput, {
  Chip: Object.assign(Chip, {
    Delete: ChipDelete,
  }),
  AddButton,
});
