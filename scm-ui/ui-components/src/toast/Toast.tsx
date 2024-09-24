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
import { createPortal } from "react-dom";
import styled from "styled-components";
import { getTheme, Themeable, ToastThemeContext, Type } from "./themes";
import usePortalRootElement from "../usePortalRootElement";

type Props = {
  type: Type;
  title: string;
};

const Container = styled.div<Themeable>`
  z-index: 99999;
  position: fixed;
  padding: 1.5rem;
  right: 1.5rem;
  bottom: 1.5rem;
  color: ${(props) => props.theme.primary};
  background-color: ${(props) => props.theme.secondary};
  max-width: 18rem;
  font-size: 0.75rem;
  border-radius: 5px;
  animation: 0.5s slide-up;

  & > p {
    margin-bottom: 0.5rem;
  }

  @keyframes slide-up {
    from {
      bottom: -10rem;
    }
    to {
      bottom: 1.5rem;
    }
  }
`;

const Toast: FC<Props> = ({ children, title, type }) => {
  const rootElement = usePortalRootElement("toastRoot");
  if (!rootElement) {
    // portal not yet ready
    return null;
  }

  const theme = getTheme(type);
  const content = (
    <Container theme={theme}>
      <h1 className="mb-1 has-text-weight-bold">{title}</h1>
      <ToastThemeContext.Provider value={theme}>{children}</ToastThemeContext.Provider>
    </Container>
  );

  return createPortal(content, rootElement);
};

export default Toast;
