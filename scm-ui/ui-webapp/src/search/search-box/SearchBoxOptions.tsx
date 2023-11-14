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
import React, { HTMLAttributes, useContext, useRef } from "react";
import { useTranslation } from "react-i18next";
import { SearchBoxContext } from "./SearchBoxContext";
import { useOverlayPosition } from "react-aria";
import { Portal } from "@radix-ui/react-portal";
import classNames from "classnames";
import { mergeRefs } from "./mergeRefs";

export const NoOptionsMenuItem = styled.li`
  line-height: inherit;
  word-break: break-all;
  display: none;

  &:only-child {
    display: inline-block;
  }
`;

const SeachBoxOptionsContainer = styled.ul`
  border: var(--scm-border);
  background-color: var(--scm-secondary-background);
  max-width: 35ch;
  overflow-y: auto;

  &[hidden] {
    display: none !important;
  }
`;

export const SearchBoxOptions = React.forwardRef<
  HTMLUListElement,
  Omit<HTMLAttributes<HTMLUListElement>, "role" | "tabIndex" | "id" | "hidden">
>(({ children, className, ...props }, ref) => {
  const [t] = useTranslation("commons");
  const { open, popupId, inputRef } = useContext(SearchBoxContext);
  const innerRef = useRef<HTMLUListElement>(null);
  const { overlayProps } = useOverlayPosition({
    overlayRef: innerRef,
    targetRef: inputRef,
    offset: 8,
    placement: "bottom left",
    isOpen: open,
  });

  return (
    <Portal asChild>
      <SeachBoxOptionsContainer
        {...props}
        className={classNames(
          "is-flex is-flex-direction-column has-rounded-border has-box-shadow is-overflow-hidden",
          className
        )}
        ref={mergeRefs(innerRef, ref)}
        role="menu"
        tabIndex={-1}
        id={popupId}
        hidden={!open}
        {...overlayProps}
      >
        <NoOptionsMenuItem
          className="px-3 py-2 has-text-inherit is-size-6 is-borderless has-background-transparent is-full-width"
          role="menuitem"
          aria-disabled={true}
        >
          {t("form.searchBox.noOptions")}
        </NoOptionsMenuItem>
        {children}
      </SeachBoxOptionsContainer>
    </Portal>
  );
});
