import React, { ReactNode } from "react";
import { Link, Route } from "react-router-dom";
import classNames from "classnames";

type Props = {
  to: string;
  icon?: string;
  label: string;
  activeOnlyWhenExact?: boolean;
  activeWhenMatch?: (route: any) => boolean;
  children?: ReactNode;
  collapsed?: boolean;
  onCollapsed?: (newStatus: boolean) => void;
  title?: string
};

class SubNavigation extends React.Component<Props> {
  static defaultProps = {
    activeOnlyWhenExact: false
  };

  isActive(route: any) {
    const { activeWhenMatch } = this.props;
    return route.match || (activeWhenMatch && activeWhenMatch(route));
  }

  renderLink = (route: any) => {
    const { to, icon, label, collapsed, title } = this.props;

    let defaultIcon = "fas fa-cog";
    if (icon) {
      defaultIcon = icon;
    }

    let children = null;
    if (this.isActive(route)) {
      children = <ul className="sub-menu">{this.props.children}</ul>;
    }

    return (
      <li title={collapsed ? title : undefined}>
        <Link
          className={classNames(this.isActive(route) ? "is-active" : "", collapsed ? "has-text-centered" : "")}
          to={to}
        >
          <i className={classNames(defaultIcon, "fa-fw")} /> {collapsed ? "" : label}
        </Link>
        {children}
      </li>
    );
  };

  render() {
    const { to, activeOnlyWhenExact } = this.props;

    // removes last part of url
    const parents = to.split("/");
    parents.splice(-1, 1);
    const parent = parents.join("/");

    return <Route path={parent} exact={activeOnlyWhenExact} children={this.renderLink} />;
  }
}

export default SubNavigation;
