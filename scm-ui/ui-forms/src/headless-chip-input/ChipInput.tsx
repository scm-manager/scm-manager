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
  createContext,
  HTMLAttributes,
  InputHTMLAttributes,
  KeyboardEventHandler,
  LiHTMLAttributes,
  useCallback,
  useContext,
  useMemo,
} from "react";
import { Slot } from "@radix-ui/react-slot";

type ChipInputContextType = {
  add(newValue: string): void;
  remove(index: number): void;
  disabled?: boolean;
  readOnly?: boolean;
  value: string[];
};
const ChipInputContext = createContext<ChipInputContextType>(null as unknown as ChipInputContextType);

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
  asChild?: boolean;
} & Omit<InputHTMLAttributes<HTMLInputElement>, "onKeyDown" | "disabled" | "readOnly">;

/**
 * @beta
 * @since 2.44.0
 */
export const NewChipInput = React.forwardRef<HTMLInputElement, NewChipInputProps>(({ asChild, ...props }, ref) => {
  const { add, value, disabled, readOnly } = useContext(ChipInputContext);
  const handleKeyDown = useCallback<KeyboardEventHandler<HTMLInputElement>>(
    (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        const newValue = e.currentTarget.value.trim();
        if (newValue && !value?.includes(newValue)) {
          add(newValue);
          e.currentTarget.value = "";
        }
        return false;
      }
    },
    [add, value]
  );
  const Comp = asChild ? Slot : "input";
  return <Comp {...props} onKeyDown={handleKeyDown} readOnly={readOnly} disabled={disabled} ref={ref} />;
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

type Props = {
  value?: string[];
  onChange?: (newValue: string[]) => void;
  readOnly?: boolean;
  disabled?: boolean;
} & Omit<HTMLAttributes<HTMLUListElement>, "onChange">;

/**
 * @beta
 * @since 2.44.0
 */
const ChipInput = React.forwardRef<HTMLUListElement, Props>(
  ({ children, value = [], disabled, readOnly, onChange, ...props }, ref) => {
    const isInactive = useMemo(() => disabled || readOnly, [disabled, readOnly]);
    const add = useCallback(
      (newValue: string) => !isInactive && onChange && onChange([...value, newValue]),
      [isInactive, onChange, value]
    );
    const remove = useCallback(
      (index: number) => !isInactive && onChange && onChange(value?.filter((_, itdx) => itdx !== index)),
      [isInactive, onChange, value]
    );
    return (
      <ChipInputContext.Provider
        value={useMemo(
          () => ({
            value,
            disabled,
            readOnly,
            add,
            remove,
          }),
          [add, disabled, readOnly, remove, value]
        )}
      >
        <ul {...props} ref={ref}>
          {children}
        </ul>
      </ChipInputContext.Provider>
    );
  }
);

export default Object.assign(ChipInput, {
  Chip: Object.assign(Chip, {
    Delete: ChipDelete,
  }),
});
