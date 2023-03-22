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

import styled from "styled-components";
// @ts-ignore react-diff-view does not provide types
import { Hunk } from "react-diff-view";

export type Collapsible = {
  collapsed?: boolean;
};

export const StyledHunk = styled(Hunk)`
  ${(props) => {
    let style = props.icon
      ? `
    .diff-gutter:hover::after {
      font-size: inherit;
      margin-left: 0.5em;
      font-family: "Font Awesome 5 Free";
      content: "${props.icon}";
      color: var(--scm-column-selection);
    }
  `
      : "";
    if (!props.actionable) {
      style += `
      .diff-gutter {
        cursor: default;
      }
    `;
    }
    if (props.highlightLineOnHover) {
      style += `
      tr.diff-line:hover > td {
        background-color: var(--sh-selected-color);
      }
    `;
    }
    return style;
  }}
`;

export const DiffFilePanel = styled.div<Collapsible>`
  /* remove bottom border for collapsed panels */
  ${(props: Collapsible) => (props.collapsed ? "border-bottom: none;" : "")};
`;

export const FullWidthTitleHeader = styled.div`
  max-width: 100%;
`;

export const MarginlessModalContent = styled.div`
  margin: -1.25rem;

  & .panel-block {
    flex-direction: column;
    align-items: stretch;
  }
`;

export const PanelHeading = styled.div<{ sticky?: boolean }>`
  ${(props) =>
    props.sticky
      ? `
   position: sticky;
   top: 52px;
   z-index: 1;
  `
      : ""}
`;
