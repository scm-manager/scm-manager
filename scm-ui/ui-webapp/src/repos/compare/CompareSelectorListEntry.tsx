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
        "is-active": isSelected
      })}
      onClick={onClick}
    >
      {children}
    </FocusButton>
  </li>
);

export default CompareSelectorListEntry;
