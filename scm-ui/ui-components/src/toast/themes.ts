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
    tertiary: "white"
  },
  primary: {
    primary: "#363636",
    secondary: "#7fe8ef",
    tertiary: "white"
  },
  success: {
    primary: "#363636",
    secondary: "#7fe3cd",
    tertiary: "white"
  },
  warning: {
    primary: "#905515",
    secondary: "#ffeeab",
    tertiary: "white"
  },
  danger: {
    primary: "#363636",
    secondary: "#ff9baf",
    tertiary: "white"
  }
};

export const getTheme = (name: Type) => {
  const theme = themes[name];
  if (!theme) {
    throw new Error(`could not find theme with name ${name}`);
  }
  return theme;
};

export const ToastThemeContext = React.createContext(themes.warning);
