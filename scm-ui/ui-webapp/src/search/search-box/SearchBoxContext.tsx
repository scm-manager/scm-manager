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

import React, { FocusEventHandler, KeyboardEventHandler, MouseEventHandler, RefObject } from "react";

export type SearchBoxContextType = {
  query: string;
  onQueryChange: (newQuery: string) => void;
  open: boolean;
  popupId: string;
  handleInputBlur: FocusEventHandler<HTMLInputElement>;
  handleInputFocus: FocusEventHandler<HTMLInputElement>;
  handleInputKeyDown: KeyboardEventHandler<HTMLInputElement>;
  handleOptionMouseEnter: MouseEventHandler<HTMLAnchorElement>;
  activeId?: string;
  registerOption: (ref: RefObject<HTMLAnchorElement>) => void;
  deregisterOption: (ref: RefObject<HTMLAnchorElement>) => void;
  inputRef: RefObject<HTMLInputElement>;
};
export const SearchBoxContext = React.createContext<SearchBoxContextType>(null as unknown as SearchBoxContextType);
