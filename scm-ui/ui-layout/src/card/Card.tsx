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

import React, { ComponentType, HTMLAttributes, ReactHTML, Ref } from "react";
import styled from "styled-components";
import classNames from "classnames";

const CardRowsContainer = styled.div`
  overflow: hidden;
`;

type Props = HTMLAttributes<HTMLElement> & {
  action?: React.ReactElement;
  /**
   * @default 'div'
   */
  as?: keyof ReactHTML | ComponentType<HTMLAttributes<HTMLElement> & { ref?: Ref<HTMLElement> }>;
};

/**
 * @beta
 * @since 2.44.0
 */
const Card = React.forwardRef<HTMLElement, Props>(({ className, children, as: Comp = "div", action, ...props }, ref) =>
  React.createElement(
    Comp,
    {
      className: classNames(className, "is-relative", "is-flex", "scmm-card"),
      ref,
      ...props,
    },
    <CardRowsContainer className="is-flex is-flex-direction-column is-justify-content-center is-flex-grow-1">
      {children}
    </CardRowsContainer>,
    action ? <span className="ml-2">{action}</span> : null
  )
);

export default Card;
