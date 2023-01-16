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

import React, { ReactElement, ReactNode } from "react";
import * as RadixTooltip from "@radix-ui/react-tooltip";
import classNames from "classnames";
import styled from "styled-components";

const StyledContent = styled(RadixTooltip.Content)`
  overflow: hidden;
  hyphens: auto;
  text-overflow: clip;
  white-space: pre-wrap;
  max-width: 15rem;
  word-break: keep-all;
  z-index: 1020;
`;

const StyledArrow = styled(RadixTooltip.Arrow)`
  z-index: 1020;
`;

type Props = {
  /**
   * The message to be displayed in the overlay.
   */
  message: ReactNode;

  /**
   * It is required to provide a {@link ReactElement} as the single child because listeners and metadata is going to be
   * attached to it automatically.
   */
  children: ReactElement;

  /**
   * Class to be applied to the content container.
   */
  className?: string;
} & Pick<RadixTooltip.TooltipContentProps, "side">;

/**
 * Displays the given {@link Props#message} whenever the provided {@link Props#children} is hovered or focused.
 *
 * @since 2.41.0
 * @beta
 */
const Tooltip = React.forwardRef<HTMLDivElement, Props>(({ children, className, message, side }, ref) => (
  <RadixTooltip.Provider>
    <RadixTooltip.Root>
      <RadixTooltip.Trigger asChild>{children}</RadixTooltip.Trigger>
      <RadixTooltip.Portal>
        <StyledContent
          className={classNames(
            "is-size-7",
            "is-family-primary",
            "has-rounded-border",
            "has-text-white",
            "has-background-grey-dark",
            "has-text-weight-semibold",
            "p-2",
            className
          )}
          side={side}
          sideOffset={5}
          ref={ref}
        >
          {message}
          <StyledArrow className="tooltip-arrow-fill-color" />
        </StyledContent>
      </RadixTooltip.Portal>
    </RadixTooltip.Root>
  </RadixTooltip.Provider>
));
export default Tooltip;
