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

import React, { ComponentProps, ReactNode, useState } from "react";
import * as RadixCollapsible from "@radix-ui/react-collapsible";
import { Icon } from "@scm-manager/ui-buttons";
import styled from "styled-components";

const StyledTrigger = styled(RadixCollapsible.Trigger)`
  margin-right: 0.5rem;
`;

const StyledCollapsibleHeader = styled.div`
  background-color: var(--scm-secondary-less-color);
`;

type Props = {
  header: ReactNode;
} & Pick<ComponentProps<typeof RadixCollapsible.Root>, "defaultOpen">;

/**
 * @beta;
 * @since 2.46.0
 */
const Collapsible = React.forwardRef<HTMLButtonElement, Props>(({ children, header, defaultOpen }, ref) => {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <RadixCollapsible.Root className="card" open={open} onOpenChange={setOpen} defaultOpen={defaultOpen}>
      <StyledCollapsibleHeader className="card-header is-flex is-justify-content-space-between is-shadowless">
        <span className="card-header-title">{header}</span>
        <StyledTrigger className="card-header-icon" ref={ref}>
          <Icon>{open ? "angle-up" : "angle-down"}</Icon>
        </StyledTrigger>
      </StyledCollapsibleHeader>
      <RadixCollapsible.Content className="card-content p-2">{children}</RadixCollapsible.Content>
    </RadixCollapsible.Root>
  );
});

export default Collapsible;
