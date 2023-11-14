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

import React, { FocusEventHandler, InputHTMLAttributes, KeyboardEventHandler, useCallback, useContext } from "react";
import { SearchBoxContext } from "./SearchBoxContext";
import { mergeRefs } from "./mergeRefs";
import classNames from "classnames";

export const SearchBoxInput = React.forwardRef<
  HTMLInputElement,
  Omit<
    InputHTMLAttributes<HTMLInputElement>,
    "type" | "tabIndex" | "aria-haspopup" | "autoComplete" | "aria-controls" | "aria-activedescendant"
  >
>(({ className, onKeyDown, onBlur, onFocus, ...props }, ref) => {
  const { onQueryChange, query, popupId, handleInputKeyDown, handleInputFocus, handleInputBlur, activeId, inputRef } =
    useContext(SearchBoxContext);
  const handleKeyDown: KeyboardEventHandler<HTMLInputElement> = useCallback(
    (e) => {
      handleInputKeyDown(e);
      onKeyDown?.(e);
    },
    [handleInputKeyDown, onKeyDown]
  );
  const handleFocus: FocusEventHandler<HTMLInputElement> = useCallback(
    (e) => {
      handleInputFocus(e);
      onFocus?.(e);
    },
    [handleInputFocus, onFocus]
  );
  const handleBlur: FocusEventHandler<HTMLInputElement> = useCallback(
    (e) => {
      handleInputBlur(e);
      onBlur?.(e);
    },
    [handleInputBlur, onBlur]
  );
  return (
    <input
      value={query}
      onChange={(e) => onQueryChange?.(e.target.value)}
      {...props}
      ref={mergeRefs(inputRef, ref)}
      type="text"
      aria-haspopup="menu"
      autoComplete="off"
      aria-controls={popupId}
      aria-activedescendant={activeId}
      onKeyDown={handleKeyDown}
      onFocus={handleFocus}
      onBlur={handleBlur}
      className={classNames("input", className)}
    />
  );
});
