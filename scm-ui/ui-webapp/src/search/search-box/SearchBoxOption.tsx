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
