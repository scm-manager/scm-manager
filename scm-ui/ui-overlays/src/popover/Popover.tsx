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
import * as RadixPopover from "@radix-ui/react-popover";
import styled from "styled-components";
import classNames from "classnames";
import { useTranslation } from "react-i18next";

const StyledContent = styled(RadixPopover.Content)`
  z-index: 1020;
`;

const StyledArrow = styled(RadixPopover.Arrow)`
  fill: var(--scm-popover-border-color);
  z-index: 1020;
`;

const TitleContainer = styled("div")`
  flex: 1;
`;

type Props = {
  /**
   * Element to trigger the popover
   */
  trigger: ReactElement;

  /**
   * Element for the content of the popover
   */
  children: ReactElement;

  /**
   * Element for the title row of the popover
   */
  title: ReactNode;

  /**
   * Classnames for the content of the popover
   */
  className?: string;
};

/**
 * @beta
 * @since 2.46.0
 */
const Popover = React.forwardRef<HTMLDivElement, Props>(({ title, className, trigger, children }, ref) => {
  const [t] = useTranslation("commons");

  return (
    <RadixPopover.Root>
      <RadixPopover.Trigger asChild>{trigger}</RadixPopover.Trigger>
      <RadixPopover.Portal>
        <StyledContent
          ref={ref}
          className={classNames("has-rounded-border", "p-2", "popover-content", "box", "popover", className)}
        >
          <div className="is-flex">
            <TitleContainer>{title}</TitleContainer>
            <RadixPopover.Close asChild>
              <button className="delete popover-close" aria-label={t("popover.closeButton.ariaLabel")} />
            </RadixPopover.Close>
          </div>
          {children}
          <StyledArrow className="popover-arrow" />
        </StyledContent>
      </RadixPopover.Portal>
    </RadixPopover.Root>
  );
});

export default Popover;
