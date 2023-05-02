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
import styled from "styled-components";
import classNames from "classnames";

const CardTitleElement = styled.h3`
  a {
    color: var(--scm-secondary-text);

    &:focus {
      outline: none;
      &::after {
        outline: #af3ee7 3px solid;
        outline-offset: 0px;
      }
    }

    &:hover::after {
      background-color: var(--scm-hover-color-blue);
    }

    &::after {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      bottom: 0;
      right: 0;
      border-radius: 4px;
    }
  }
`;

type Props = HTMLAttributes<HTMLHeadingElement>;

/**
 * A card title may contain a link as its only child which will be automatically stretched to cover the whole card area.
 *
 * If a card title has a link, individual card elements which should be interactive have to get the `is-relative` class.
 *
 * The card title (or enclosed link) content must be an accessible text and must not contain any other interactive elements.
 *
 * You can wrap the title in a {@link CardList.Card.Row} to introduce other elements next to the title.
 *
 * The title (or its enclosing row) must be the first element in a {@link CardList.Card}.
 *
 * @beta
 * @since 2.44.0
 */
const CardTitle = React.forwardRef<HTMLHeadingElement, Props>(({ children, className, ...props }, ref) => (
  <CardTitleElement className={classNames(className, "is-ellipsis-overflow")} ref={ref} {...props}>
    {children}
  </CardTitleElement>
));

export default CardTitle;
