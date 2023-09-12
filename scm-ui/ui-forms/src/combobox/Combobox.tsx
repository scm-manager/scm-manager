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
  ForwardedRef,
  Fragment,
  KeyboardEvent,
  KeyboardEventHandler,
  ReactElement,
  Ref,
  useEffect,
  useRef,
  useState,
} from "react";
import { Combobox as HeadlessCombobox } from "@headlessui/react";
import classNames from "classnames";
import styled from "styled-components";
import { withForwardRef } from "../helpers";
import { createAttributesForTesting } from "@scm-manager/ui-components";
import { Option } from "@scm-manager/ui-types";

const OptionsWrapper = styled(HeadlessCombobox.Options).attrs({
  className: "is-flex is-flex-direction-column has-rounded-border has-box-shadow is-overflow-hidden",
})`
  z-index: 400;
  top: 2.5rem;
  border: var(--scm-border);
  background-color: var(--scm-secondary-background);
  max-width: 35ch;

  &:empty {
    border: 0;
    clip: rect(0 0 0 0);
    height: 1px;
    margin: -1px;
    overflow: hidden;
    padding: 0;
    position: absolute;
    white-space: nowrap;
    width: 1px;
  }
`;

const StyledComboboxOption = styled.li.attrs({
  className: "px-3 py-2 has-text-inherit is-clickable is-size-6 is-borderless has-background-transparent",
})<{ isActive: boolean }>`
  line-height: inherit;
  background-color: ${({ isActive }) => (isActive ? "var(--scm-column-selection)" : "")};
  word-break: break-all;
  &[data-disabled] {
    color: unset !important;
    opacity: 40%;
    cursor: unset !important;
  }
`;

type BaseProps<T> = {
  className?: string;
  onKeyDown?: KeyboardEventHandler<HTMLElement>;
  value?: Option<T> | null;
  onChange?: (value?: Option<T>) => void;
  onBlur?: () => void;
  disabled?: boolean;
  defaultValue?: Option<T>;
  nullable?: boolean;
  readOnly?: boolean;
  onQueryChange?: (value: string) => void;
  id?: string;
  placeholder?: string;
  "aria-describedby"?: string;
  "aria-labelledby"?: string;
  "aria-label"?: string;
  testId?: string;
  ref?: Ref<HTMLInputElement>;
  form?: string;
  name?: string;
};

export type ComboboxProps<T> =
  | (BaseProps<T> & {
      options: Array<Option<T>> | ((query: string) => Array<Option<T>> | Promise<Array<Option<T>>>);
      children?: (option: Option<T>, index: number) => ReactElement;
    })
  | (BaseProps<T> & { children: Array<ReactElement | null>; options?: never });

/**
 * @beta
 * @since 2.45.0
 */
function ComboboxComponent<T>(props: ComboboxProps<T>, ref: ForwardedRef<HTMLInputElement>) {
  const [query, setQuery] = useState("");
  const { onQueryChange } = props;

  useEffect(() => onQueryChange && onQueryChange(query), [onQueryChange, query]);

  let options;

  if (Array.isArray(props.children)) {
    options = props.children;
  } else if (typeof props.options === "function") {
    options = <ComboboxOptions<T> children={props.children} options={props.options} query={query} />;
  } else {
    options = props.options?.map((o, index) =>
      typeof props.children === "function" ? (
        props.children(o, index)
      ) : (
        <HeadlessCombobox.Option value={o} key={o.label} as={Fragment}>
          {({ active }) => <StyledComboboxOption isActive={active}>{o.displayValue ?? o.label}</StyledComboboxOption>}
        </HeadlessCombobox.Option>
      )
    );
  }

  return (
    <HeadlessCombobox
      as="div"
      value={props.value}
      onChange={(e?: Option<T>) => props.onChange && props.onChange(e)}
      disabled={props.disabled || props.readOnly}
      onKeyDown={(e: KeyboardEvent<HTMLElement>) => props.onKeyDown && props.onKeyDown(e)}
      name={props.name}
      form={props.form}
      defaultValue={props.defaultValue}
      // @ts-ignore
      nullable={props.nullable}
      className="is-relative is-flex-grow-1 is-flex"
      by="value"
    >
      <HeadlessCombobox.Input<Option<T>>
        displayValue={(o) => o?.label}
        ref={ref}
        onChange={(e) => setQuery(e.target.value)}
        className={classNames(props.className, "is-full-width", "input", "is-ellipsis-overflow")}
        aria-describedby={props["aria-describedby"]}
        aria-labelledby={props["aria-labelledby"]}
        aria-label={props["aria-label"]}
        id={props.id}
        placeholder={props.placeholder}
        onBlur={props.onBlur}
        autoComplete="off"
        {...createAttributesForTesting(props.testId)}
      />
      <OptionsWrapper className="is-absolute">{options}</OptionsWrapper>
    </HeadlessCombobox>
  );
}

type ComboboxOptionsProps<T> = {
  options: (query: string) => Array<Option<T>> | Promise<Array<Option<T>>>;
  children?: (option: Option<T>, index: number) => ReactElement;
  query: string;
};

function ComboboxOptions<T>({ query, options, children }: ComboboxOptionsProps<T>) {
  const [optionsData, setOptionsData] = useState<Array<Option<T>>>([]);
  const activePromise = useRef<Promise<Array<Option<T>>>>();

  useEffect(() => {
    const optionsExec = options(query);
    if (optionsExec instanceof Promise) {
      activePromise.current = optionsExec;
      optionsExec
        .then((newOptions) => {
          if (activePromise.current === optionsExec) {
            setOptionsData(newOptions);
          }
        })
        .catch(() => {
          if (activePromise.current === optionsExec) {
            setOptionsData([]);
          }
        });
    } else {
      setOptionsData(optionsExec);
    }
  }, [options, query]);

  return (
    <>
      {optionsData?.map((o, index) =>
        typeof children === "function" ? (
          children(o, index)
        ) : (
          <HeadlessCombobox.Option value={o} key={o.label} as={Fragment}>
            {({ active }) => <StyledComboboxOption isActive={active}>{o.displayValue ?? o.label}</StyledComboboxOption>}
          </HeadlessCombobox.Option>
        )
      )}
    </>
  );
}

const Combobox = Object.assign(withForwardRef(ComboboxComponent), {
  Option: StyledComboboxOption,
});

export default Combobox;
