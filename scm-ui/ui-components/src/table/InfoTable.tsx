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

const InfoTable: FC<Props> = ({ className, children, ...rest }) => (
  <StyledTable className={classNames("table", className)} {...rest}>
    {children}
  </StyledTable>
);
export default InfoTable;
