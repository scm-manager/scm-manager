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
import React, { FC } from "react";
import styled from "styled-components";
import useMenuContext from "../navigation/MenuContext";

const SecondaryColumn = styled.div<{ collapsed: boolean }>`
  /* In Bulma there is unfortunately no intermediate step between .is-1 and .is-2, hence the size. 
  Navigation size should be as constant as possible. */
  flex: none;
  width: ${(props) => (props.collapsed ? "5.5rem" : "20.5rem")};
  max-width: ${(props: { collapsed: boolean }) => (props.collapsed ? "11.3%" : "25%")};
  /* Render this column to full size if column construct breaks (page size too small). */
  @media (max-width: 785px) {
    width: 100%;
    max-width: 100%;
  }
`;

const SecondaryNavigationColumn: FC = ({ children }) => {
  const context = useMenuContext();
  return (
    <SecondaryColumn className="column" collapsed={context.isCollapsed()}>
      {children}
    </SecondaryColumn>
  );
};

export default SecondaryNavigationColumn;
