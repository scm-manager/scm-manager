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

import React, { ComponentProps } from "react";
import { Button, Icon } from "../../buttons";
import * as RadixMenu from "@radix-ui/react-dropdown-menu";
import { useTranslation } from "react-i18next";
import classNames from "classnames";

type Props = ComponentProps<typeof Button>;

/**
 * @beta
 * @since 2.44.0
 */
const MenuTrigger = React.forwardRef<HTMLButtonElement, Props>(({ children, ...props }, ref) => (
  <RadixMenu.Trigger asChild>
    <Button ref={ref} {...props}>
      {children}
    </Button>
  </RadixMenu.Trigger>
));

/**
 * @beta
 * @since 2.44.0
 */
export const DefaultMenuTrigger = React.forwardRef<HTMLButtonElement, Props>(({ className, ...props }, ref) => {
  const [t] = useTranslation("commons");
  return (
    <MenuTrigger
      aria-label={t("menu.defaultTriggerLabel")}
      className={classNames(className, "is-borderless has-background-transparent has-hover-color-blue px-2")}
      ref={ref}
      {...props}
    >
      <Icon>ellipsis-v</Icon>
    </MenuTrigger>
  );
});

export default MenuTrigger;
