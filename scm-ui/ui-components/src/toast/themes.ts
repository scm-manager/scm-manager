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

import * as React from "react";

export type ToastTheme = {
  primary: string;
  secondary: string;
  tertiary: string;
};

export type Themeable = {
  theme: ToastTheme;
};

export const types = ["info", "primary", "success", "warning", "danger"] as const;

export type Type = typeof types[number];

const themes: { [name in Type]: ToastTheme } = {
  info: {
    primary: "#363636",
    secondary: "#99d8f3",
    tertiary: "white",
  },
  primary: {
    primary: "#363636",
    secondary: "#7fe8ef",
    tertiary: "white",
  },
  success: {
    primary: "#363636",
    secondary: "#7fe3cd",
    tertiary: "white",
  },
  warning: {
    primary: "#905515",
    secondary: "#ffeeab",
    tertiary: "white",
  },
  danger: {
    primary: "#363636",
    secondary: "#ff9baf",
    tertiary: "white",
  },
};

export const getTheme = (name: Type) => {
  const theme = themes[name];
  if (!theme) {
    throw new Error(`could not find theme with name ${name}`);
  }
  return theme;
};

export const ToastThemeContext = React.createContext(themes.warning);
