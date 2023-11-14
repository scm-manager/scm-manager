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
