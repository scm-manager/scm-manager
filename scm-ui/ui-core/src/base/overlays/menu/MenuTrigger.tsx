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
