import React, { FC, ReactElement, useContext } from "react";
import { Link, Route } from "react-router-dom";
import classNames from "classnames";
import { MenuContext } from "./MenuContext";

type Props = {
  to: string;
  icon?: string;
  label: string;
  activeOnlyWhenExact?: boolean;
  activeWhenMatch?: (route: any) => boolean;
  children?: ReactElement[];
  collapsed?: boolean;
  title?: string;
};

const SubNavigation: FC<Props> = ({
  to,
  icon,
  label,
  activeOnlyWhenExact,
  activeWhenMatch,
  children,
  collapsed,
  title
}) => {
  const menuContext = useContext(MenuContext);

  const isActive = (route: any) => {
    return route.match || activeWhenMatch && activeWhenMatch(route);
  };

  const renderLink = (route: any) => {
    let defaultIcon = "fas fa-cog";
    if (icon) {
      defaultIcon = icon;
    }

    let childrenList = null;
    if (isActive(route)) {
      if (menuContext.menuCollapsed) {
        menuContext.setMenuCollapsed(false);
      }
      childrenList = <ul className="sub-menu">{children}</ul>;
    }

    return (
      <li title={collapsed ? title : undefined}>
        <Link className={classNames(isActive(route) ? "is-active" : "", collapsed ? "has-text-centered" : "")} to={to}>
          <i className={classNames(defaultIcon, "fa-fw")} /> {collapsed ? "" : label}
        </Link>
        {childrenList}
      </li>
    );
  };

  // removes last part of url
  const parents = to.split("/");
  parents.splice(-1, 1);
  const parent = parents.join("/");

  return <Route path={parent} exact={activeOnlyWhenExact} children={renderLink} />;
};

export default SubNavigation;
