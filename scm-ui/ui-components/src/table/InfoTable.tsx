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

import React, { FC, HTMLProps, ReactNode } from "react";
import styled from "styled-components";
import classNames from "classnames";
import { devices } from "../devices";

type Props = Omit<HTMLProps<HTMLTableElement>, "children" | "as" | "ref"> & {
  children: ReactNode;
};

const StyledTable = styled.table`
  @media screen and (max-width: ${devices.mobile.width}px) {
    td,
    th {
      display: block;
    }
    td {
      border-width: 0 0 1px;
      word-break: break-word;
    }
    th {
      border: none;
      padding-bottom: 0;
    }
    th:after {
      content: ": ";
    }
  }
`;

/**
 * @deprecated
 */
const InfoTable: FC<Props> = ({ className, children, ...rest }) => (
  <StyledTable className={classNames("table", className)} {...rest}>
    {children}
  </StyledTable>
);
export default InfoTable;
