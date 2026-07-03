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

import React, { FC, ReactNode, TableHTMLAttributes } from "react";
import styled from "styled-components";
import classNames from "classnames";

type Props = TableHTMLAttributes<HTMLTableElement> & {
  children: ReactNode;
};

const StyledSubpageTable = styled.table`
  &.table {
    width: 100%;
  }

  &.table td,
  &.table th {
    vertical-align: middle;
  }

  &.table tbody td {
    height: 4.75rem;
  }

  @media screen and (max-width: 768px) {
    thead {
      display: none;
    }

    tbody tr {
      display: block;
      padding: 0.75rem 0;
      border-bottom: 1px solid var(--scm-border-color, #dbdbdb);
    }

    tbody td {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      border: none;
      padding: 0.25rem 0;
      word-break: break-word;
      height: auto;
    }

    tbody td[data-label]::before {
      content: attr(data-label);
      font-weight: 600;
      color: var(--scm-secondary-text, #7a7a7a);
    }

    tbody td:empty {
      display: none;
    }
  }
`;

/**
 * A read-only table for the overview of a `SubpageForm` that stacks into blocks on narrow screens: the
 * `thead` is hidden and each `td` shows its column header from its `data-label` attribute.
 *
 * Body rows share a fixed height and vertically center their content, so rows stay uniform and a row-action
 * button (e.g. `Subpage.OpenIconButton`) lines up with the row's text.
 *
 * @beta
 * @since 4.0.0
 */
const SubpageTable: FC<Props> = ({ className, children, ...rest }) => (
  <StyledSubpageTable className={classNames("table", className)} {...rest}>
    {children}
  </StyledSubpageTable>
);

export default SubpageTable;
