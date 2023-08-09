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

import React, { ComponentProps, HTMLAttributes } from "react";
import classNames from "classnames";
import styled from "styled-components";
import Card from "../card/Card";

/**
 * @beta
 * @since 2.44.0
 * @see Card
 */
export const CardListCard = React.forwardRef<HTMLElement, Omit<ComponentProps<typeof Card>, "as">>((props, ref) => (
  <Card ref={ref} {...props} as="li" />
));

const CardListElement = styled.ul`
  > * + * {
    margin-top: calc(0.5rem + 1px);

    &::before {
      content: "";
      position: absolute;
      width: 100%;
      border-top: var(--scm-border);
      left: 0;
      top: calc(-0.25rem - 1px);
    }
  }
`;

type Props = HTMLAttributes<HTMLUListElement>;

/**
 * @beta
 * @since 2.44.0
 */
const CardList = React.forwardRef<HTMLUListElement, Props>(({ children, className, ...props }, ref) => (
  <CardListElement ref={ref} {...props} className={classNames(className, "is-flex", "is-flex-direction-column")}>
    {children}
  </CardListElement>
));

/**
 * @beta
 * @since 2.44.0
 */
export const CardListBox = React.forwardRef<HTMLUListElement, Props>(({ className, children, ...props }, ref) => (
  <CardList className={classNames(className, "p-2 box")} ref={ref} {...props}>
    {children}
  </CardList>
));

export default CardList;
