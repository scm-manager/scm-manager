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
import React, { HTMLAttributes, useContext, useRef } from "react";
import { useTranslation } from "react-i18next";
import { SearchBoxContext } from "./SearchBoxContext";
import { useOverlayPosition } from "@react-aria/overlays";
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
