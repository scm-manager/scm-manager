import React, { FC, ReactElement, useContext, useEffect } from "react";
import { Link, useRouteMatch } from "react-router-dom";
import classNames from "classnames";

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

const SubNavigation: FC<Props> = ({ to, activeOnlyWhenExact, icon, collapsed, title, label, children }) => {
  const parents = to.split("/");
  parents.splice(-1, 1);
  const parent = parents.join("/");

  const match = useRouteMatch({
    path: parent,
    exact: activeOnlyWhenExact
  });

  let defaultIcon = "fas fa-cog";
  if (icon) {
    defaultIcon = icon;
  }

  let childrenList = null;
  if (match && !collapsed) {
    childrenList = <ul className="sub-menu">{children}</ul>;
  }

  return (
    <li title={collapsed ? title : undefined}>
      <Link className={classNames(match != null ? "is-active" : "", collapsed ? "has-text-centered" : "")} to={to}>
        <i className={classNames(defaultIcon, "fa-fw")} /> {collapsed ? "" : label}
      </Link>
      {childrenList}
    </li>
  );
};

export default SubNavigation;
