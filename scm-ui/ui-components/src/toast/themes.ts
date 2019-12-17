import * as React from "react";

export type ToastTheme = {
  primary: string;
  secondary: string;
  tertiary: string;
};

export type Themeable = {
  theme: ToastTheme;
};

export type Type = "info" | "primary" | "success" | "warning" | "danger";

export const types: Type[] = ["info", "primary", "success", "warning", "danger"];

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
