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
import React, { FC, useContext, MouseEvent } from "react";
import { ToastThemeContext, Themeable } from "./themes";
import classNames from "classnames";
import styled from "styled-components";

type Props = {
  icon?: string;
  onClick?: () => void;
};

const ThemedButton = styled.button.attrs((props) => ({
  className: "button",
}))<Themeable>`
  color: ${(props) => props.theme.primary};
  border-color: ${(props) => props.theme.primary};
  background-color: ${(props) => props.theme.secondary};
  font-size: 0.75rem;

  &:hover {
    color: ${(props) => props.theme.primary};
    border-color: ${(props) => props.theme.tertiary};
    background-color: ${(props) => props.theme.tertiary};
  }
`;

const ToastButton: FC<Props> = ({ icon, onClick, children }) => {
  const theme = useContext(ToastThemeContext);

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (onClick) {
      e.preventDefault();
      onClick();
    }
  };

  return (
    <ThemedButton theme={theme} onClick={handleClick}>
      {icon && <i className={classNames("fas", "fa-fw", `fa-${icon}`, "mr-1")} />} {children}
    </ThemedButton>
  );
};

export default ToastButton;
