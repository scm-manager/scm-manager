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

import styled from "styled-components";
import { Link } from "react-router-dom";
import React, { ComponentProps, MouseEventHandler, useCallback, useContext, useEffect, useRef } from "react";
import { SearchBoxContext } from "./SearchBoxContext";
import { mergeRefs } from "./mergeRefs";
import { useGeneratedId } from "@scm-manager/ui-components";
import classNames from "classnames";

const SearchBoxOptionLink = styled(Link)<{ isActive: boolean }>`
  line-height: inherit;
  word-break: break-all;
  background-color: ${({ isActive }) => (isActive ? "var(--scm-column-selection)" : "")};
`;

export const SearchBoxOption = React.forwardRef<
  HTMLAnchorElement,
  Omit<ComponentProps<typeof Link>, "tabIndex" | "role">
>(({ children, id: propId, className, onMouseEnter, ...props }, forwardRef) => {
  const ref = useRef<HTMLAnchorElement>(null);
  const { activeId, deregisterOption, registerOption, handleOptionMouseEnter } = useContext(SearchBoxContext);
  const mergedRef = mergeRefs(ref, forwardRef);
  const id = useGeneratedId(propId);
  const isActive = activeId === id;
  const handleMouseEnter: MouseEventHandler<HTMLAnchorElement> = useCallback(
    (e) => {
      handleOptionMouseEnter(e);
      onMouseEnter?.(e);
    },
    [handleOptionMouseEnter, onMouseEnter]
  );

  useEffect(() => {
    registerOption(ref);
    return () => deregisterOption(ref);
  }, [deregisterOption, registerOption]);

  return (
    <li role="presentation">
      <SearchBoxOptionLink
        {...props}
        isActive={isActive}
        className={classNames(
          "px-3 py-2 has-text-inherit is-clickable is-size-6 is-borderless has-background-transparent is-full-width is-inline-block",
          className
        )}
        ref={mergedRef}
        tabIndex={-1}
        id={id}
        role="menuitem"
        onMouseEnter={handleMouseEnter}
      >
        {children}
      </SearchBoxOptionLink>
    </li>
  );
});
