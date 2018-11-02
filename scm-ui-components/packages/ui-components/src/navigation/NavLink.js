//@flow
import * as React from "react";
import {Link, Route} from "react-router-dom";

// TODO mostly copy of PrimaryNavigationLink

type Props = {
  to: string,
  label: string,
  activeOnlyWhenExact?: boolean,
  activeWhenMatch?: (route: any) => boolean
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
    const { to, label } = this.props;
    return (
      <li>
        <Link className={this.isActive(route) ? "is-active" : ""} to={to}>
          {label}
        </Link>
      </li>
    );
  };

  render() {
    const { to, activeOnlyWhenExact } = this.props;
    return (
      <Route path={to} exact={activeOnlyWhenExact} children={this.renderLink} />
    );
  }
}

export default NavLink;
