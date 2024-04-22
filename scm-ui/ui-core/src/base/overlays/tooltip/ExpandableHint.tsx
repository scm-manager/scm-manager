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

import React, { useState } from "react";
import Icon from "../../buttons/Icon";
import classNames from "classnames";
import styled from "styled-components";

type Props = {
  message: string;
  summary: string;
  className?: string;
}

const StyledButton = styled.button`
  border-radius: 4px !important;
  min-width: 2.5rem;  
  min-height: 24px;
  border: none;
  padding: 6px;
  line-height: 15px;
  &:not(:disabled) {
    &:hover {
      background-color: color-mix(in srgb, currentColor 25%, transparent);
    }
    &:active {
      background-color: color-mix(in srgb, currentColor 50%, transparent);
    }
  }
`;



/**
 * An a11y-friendly tooltip replacement that is an expandable hint
 *
 * @beta
 * @since 3.2.0
 */

const ExpandableHint = React.forwardRef<HTMLButtonElement, Props>(({ message, summary, className }, ref) => {
  const [isOpen, setOpen] = useState<boolean>(false);
  return (
    <>
      <StyledButton
        role="button"
        onClick={() => setOpen(!isOpen)}
        aria-expanded={isOpen}
        aria-label={summary}
        className={classNames(className, "is-flex")}
        ref={ref}
      >
          {isOpen ? <Icon className="is-small">chevron-down</Icon> : <Icon className="is-small">chevron-right</Icon>}
        {summary}
      </StyledButton>
      {isOpen ? <p className="mt-2 is-size-7">{message}</p> : undefined}
    </>
  );
});

export default ExpandableHint;
