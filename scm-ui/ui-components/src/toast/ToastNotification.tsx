/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
