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

import React, { LiHTMLAttributes } from "react";
import styled from "styled-components";
import classNames from "classnames";

const CardElement = styled.li`
  display: grid;
  grid-template-columns: minmax(0, 1fr) min-content;
  grid-template-rows: auto;
`;

const CardActionContainer = styled.span`
  grid-column: -1;
  grid-row: 1;
  margin-top: -0.5rem;
  margin-right: -0.5rem;
`;

type Props = LiHTMLAttributes<HTMLLIElement> & {
  action?: React.ReactElement;
};

/**
 * @beta
 * @since 2.44.0
 */
const Card = React.forwardRef<HTMLLIElement, Props>(({ className, children, action, ...props }, ref) => (
  <CardElement className={classNames(className, "is-relative", "p-2")} ref={ref} {...props}>
    {children}
    {action ? <CardActionContainer>{action}</CardActionContainer> : null}
  </CardElement>
));

export default Card;
