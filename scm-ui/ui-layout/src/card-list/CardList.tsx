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

import React, { HTMLAttributes } from "react";
import classNames from "classnames";
import styled from "styled-components";

const CardListElement = styled.ul`
  > * + * {
    border-top: var(--scm-border);
    margin-top: 0.5rem;
    padding-top: 1rem !important;

    *:is(h1, h2, h3, h4, h5, h6) a::after {
      top: 0.5rem !important;
    }
  }
`;

type Props = HTMLAttributes<HTMLUListElement>;

/**
 * The {@link CardList.Card.Title} is currently represented as a `h3`, which means the list can only be used on the top level of the page without breaking accessibility.
 *
 * @beta
 * @since 2.44.0
 */
const CardList = React.forwardRef<HTMLUListElement, Props>(({ children, className, ...props }, ref) => (
  <CardListElement ref={ref} {...props} className={classNames(className, "is-flex", "is-flex-direction-column")}>
    {children}
  </CardListElement>
));

/**
 * The {@link CardList.Card.Title} is currently represented as a `h3`, which means the list can only be used on the top level of the page without breaking accessibility.
 *
 * @beta
 * @since 2.44.0
 */
export const CardListBox = React.forwardRef<HTMLUListElement, Props>(({ className, children, ...props }, ref) => (
  <CardList className={classNames(className, "p-2 box")} ref={ref} {...props}>
    {children}
  </CardList>
));

export default CardList;
