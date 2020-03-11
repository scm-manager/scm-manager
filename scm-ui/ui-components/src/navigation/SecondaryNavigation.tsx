import React, { ReactElement, ReactNode } from "react";
import { MenuContext } from "./MenuContext";
import SubNavigation from "./SubNavigation";
import NavLink from "./NavLink";

type Props = {
  to: string;
  icon: string;
  label: string;
  title: string;
  activeWhenMatch?: (route: any) => boolean;
  activeOnlyWhenExact?: boolean;
  children?: ReactElement[];
};

export default class SecondaryNavigation extends React.Component<Props> {
  render() {
    const { to, icon, label, title, activeWhenMatch, activeOnlyWhenExact, children } = this.props;
    if (children) {
      return (
        <MenuContext.Consumer>
          {({ menuCollapsed }) => (
            <SubNavigation
              to={to}
              icon={icon}
              label={label}
              title={title}
              activeWhenMatch={activeWhenMatch}
              activeOnlyWhenExact={activeOnlyWhenExact}
              collapsed={menuCollapsed}
            >
              {children}
            </SubNavigation>
          )}
        </MenuContext.Consumer>
      );
    } else {
      return (
        <MenuContext.Consumer>
          {({ menuCollapsed }) => <NavLink to={to} icon={icon} label={label} title={title} collapsed={menuCollapsed} />}
        </MenuContext.Consumer>
      );
    }
  }
}
