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

import MenuComponent, { MenuButton, MenuDialog, MenuExternalLink, MenuLink } from "./menu/Menu";
import MenuTrigger, { DefaultMenuTrigger } from "./menu/MenuTrigger";
import DialogComponent, { CloseButton } from "./dialog/Dialog";

export { default as Tooltip } from "./tooltip/Tooltip";

export const Menu = Object.assign(MenuComponent, {
  Button: MenuButton,
  Link: MenuLink,
  ExternalLink: MenuExternalLink,
  DialogButton: MenuDialog,
  Trigger: MenuTrigger,
  DefaultTrigger: DefaultMenuTrigger,
});

export const Dialog = Object.assign(DialogComponent, {
  CloseButton,
});

export { default as Popover } from "./popover/Popover";
