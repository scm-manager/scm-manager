//@flow
import * as React from "react";
import { Route, Link } from "react-router-dom";

// TODO mostly copy of PrimaryNavigationLink

type Props = {
  to: string,
  label: string,
  activeOnlyWhenExact?: boolean
};

class NavLink extends React.Component<Props> {
  static defaultProps = {
    activeOnlyWhenExact: true
  };

  renderLink = (route: any) => {
    const { to, label } = this.props;
    return (
      <li>
        <Link className={route.match ? "is-active" : ""} to={to}>
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
