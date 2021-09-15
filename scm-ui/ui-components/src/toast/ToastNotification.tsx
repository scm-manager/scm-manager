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
import classNames from "classnames";
import styled from "styled-components";
import { getTheme, Themeable, ToastThemeContext, Type } from "./themes";

type Props = {
  type: Type;
  title: string;
  close?: () => void;
};

const Container = styled.div<Themeable>`
  color: ${(props) => props.theme.primary};
  background-color: ${(props) => props.theme.secondary};
  max-width: 18rem;
  border-radius: 5px;

  & > p {
    margin-bottom: 0.5rem;
  }
`;

const ToastNotification: FC<Props> = ({ children, title, type, close }) => {
  const theme = getTheme(type);
  return (
    <Container className={classNames("notification", "mt-2", "mb-0", "p-5", "is-size-7")} theme={theme}>
      {close ? <button className="delete" onClick={close} /> : null}
      <h1 className={classNames("mb-1", "has-text-weight-bold")}>{title}</h1>
      <ToastThemeContext.Provider value={theme}>{children}</ToastThemeContext.Provider>
    </Container>
  );
};

export default ToastNotification;
