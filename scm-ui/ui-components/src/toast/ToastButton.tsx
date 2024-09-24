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
