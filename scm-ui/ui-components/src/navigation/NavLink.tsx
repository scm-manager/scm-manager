import * as React from "react";
import classNames from "classnames";
import { Link, Route } from "react-router-dom";

// TODO mostly copy of PrimaryNavigationLink

type Props = {
  to: string;
  icon?: string;
  label: string;
  activeOnlyWhenExact?: boolean;
  activeWhenMatch?: (route: any) => boolean;
  collapsed: boolean;
};

class NavLink extends React.Component<Props> {
  static defaultProps = {
    activeOnlyWhenExact: true
  };

  isActive(route: any) {
    const { activeWhenMatch } = this.props;
    return route.match || (activeWhenMatch && activeWhenMatch(route));
  }

  renderLink = (route: any) => {
    const { to, icon, label, collapsed } = this.props;

    let showIcon = null;
    if (icon) {
      showIcon = (
        <>
          <i className={classNames(icon, "fa-fw")} />{" "}
        </>
      );
    }

    return (
      <li>
        <Link
          className={classNames(this.isActive(route) ? "is-active" : "", collapsed ? "has-text-centered" : "")}
          to={to}
        >
          {showIcon}
          {collapsed ? null : label}
        </Link>
      </li>
    );
  };

  render() {
    const { to, activeOnlyWhenExact } = this.props;

    return <Route path={to} exact={activeOnlyWhenExact} children={this.renderLink} />;
  }
}

export default NavLink;
