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

import React, { AnchorHTMLAttributes, ButtonHTMLAttributes, FC } from "react";
import * as RadixMenu from "@radix-ui/react-dropdown-menu";
import styled from "styled-components";
import { DefaultMenuTrigger } from "./MenuTrigger";
import classNames from "classnames";
import { Link as ReactRouterLink, LinkProps as ReactRouterLinkProps } from "react-router-dom";

const MenuContent = styled(RadixMenu.Content)`
  border: var(--scm-border);
  background-color: var(--scm-secondary-background);
`;

const MenuItem = styled(RadixMenu.Item).attrs({
  className:
    "is-flex is-align-items-center px-3 py-2 has-text-inherit is-clickable is-size-6 has-hover-color-blue is-borderless has-background-transparent has-rounded-border",
})`
  line-height: inherit;
  :focus {
    outline: #af3ee7 3px solid;
    outline-offset: 0px;
  }
  &[data-disabled] {
    color: unset !important;
    opacity: 40%;
    cursor: unset !important;
  }
`;

type MenuLinkProps = Omit<ReactRouterLinkProps, "onSelect"> & Pick<RadixMenu.MenuItemProps, "disabled">;

/**
 * @beta
 * @since 2.44.0
 */
export const MenuLink = React.forwardRef<HTMLAnchorElement, MenuLinkProps>(({ children, disabled, ...props }, ref) => (
  <MenuItem asChild disabled={disabled}>
    <ReactRouterLink ref={ref} {...props}>
      {children}
    </ReactRouterLink>
  </MenuItem>
));

type MenuExternalLinkProps = Omit<AnchorHTMLAttributes<HTMLAnchorElement>, "onSelect"> &
  Pick<RadixMenu.MenuItemProps, "disabled">;

/**
 * External links open in a new browser tab with rel flags "noopener" and "noreferrer" set by default.
 *
 * @beta
 * @since 2.44.0
 */
export const MenuExternalLink = React.forwardRef<HTMLAnchorElement, MenuExternalLinkProps>(
  ({ children, disabled, ...props }, ref) => (
    <MenuItem asChild disabled={disabled}>
      <a ref={ref} target="_blank" rel="noopener noreferrer" {...props}>
        {children}
      </a>
    </MenuItem>
  )
);

type MenuButtonProps = Omit<ButtonHTMLAttributes<HTMLButtonElement>, "onClick" | "onSelect"> &
  Pick<RadixMenu.MenuItemProps, "onSelect">;

/**
 * Use {@link MenuButtonProps#onSelect} to handle menu item selection.
 * The menu will close by default. This behavior can be overwritten by calling {@link Event.preventDefault} on the event.
 *
 * @beta
 * @since 2.44.0
 */
export const MenuButton = React.forwardRef<HTMLButtonElement, MenuButtonProps>(
  ({ children, onSelect, disabled, ...props }, ref) => (
    <MenuItem asChild disabled={disabled} onSelect={onSelect}>
      <button ref={ref} {...props}>
        {children}
      </button>
    </MenuItem>
  )
);

type Props = {
  className?: string;
  trigger?: React.ReactElement;
} & Pick<RadixMenu.DropdownMenuContentProps, "side">;

/**
 * A menu consists of a trigger button (vertical ellipsis icon button by default)
 * and a dropdown container containing individual menu items.
 *
 * @beta
 * @since 2.44.0
 * @see https://www.w3.org/WAI/ARIA/apg/patterns/menubar/
 */
const Menu: FC<Props> = ({ children, side, className, trigger = <DefaultMenuTrigger /> }) => {
  return (
    <RadixMenu.Root>
      {trigger}
      <RadixMenu.Portal>
        <MenuContent
          className={classNames(className, "is-flex is-flex-direction-column has-rounded-border has-box-shadow")}
          side={side}
          sideOffset={4}
          collisionPadding={4}
        >
          {children}
        </MenuContent>
      </RadixMenu.Portal>
    </RadixMenu.Root>
  );
};

export default Menu;
