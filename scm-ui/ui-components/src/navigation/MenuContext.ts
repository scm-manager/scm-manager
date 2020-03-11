import React from "react";

const MENU_COLLAPSED = "secondary-menu-collapsed";

export const MenuContext = React.createContext({
  menuCollapsed: isMenuCollapsed(),
  setMenuCollapsed: (collapsed: boolean) => {}
});

export function isMenuCollapsed() {
  return localStorage.getItem(MENU_COLLAPSED) === "true";
}
export function storeMenuCollapsed(status: boolean) {
  localStorage.setItem(MENU_COLLAPSED, String(status));
}
