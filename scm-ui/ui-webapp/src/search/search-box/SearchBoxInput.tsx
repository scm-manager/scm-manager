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
