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

import React, { FC, ReactNode } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { NoStyleButton } from "@scm-manager/ui-components";

type Props = {
  children: ReactNode;
  isSelected?: boolean;
  onClick?: (event: React.MouseEvent) => void;
};

const FocusButton = styled(NoStyleButton)`
  border-radius: 0.25rem;

  &:focus:not(.is-active) {
    background-color: var(--scm-column-selection) !important;
  }
`;

const CompareSelectorListEntry: FC<Props> = ({ children, isSelected = false, onClick }) => (
  <li role="option" aria-selected={isSelected}>
    <FocusButton
      className={classNames("dropdown-item", "is-flex", "has-text-weight-medium", "px-4", "py-2", {
        "is-active": isSelected,
      })}
      onClick={onClick}
    >
      {children}
    </FocusButton>
  </li>
);

export default CompareSelectorListEntry;
