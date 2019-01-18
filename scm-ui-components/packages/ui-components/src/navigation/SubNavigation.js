//@flow
import * as React from "react";
import {Link, Route} from "react-router-dom";

type Props = {
  to: string,
  label: string,
  activeOnlyWhenExact?: boolean,
  activeWhenMatch?: (route: any) => boolean,
  children?: React.Node
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
    const { to, label } = this.props;

    let children = null;
    if(this.isActive(route)) {
      children = (
        <ul>{this.props.children}</ul>
      );
    }

    return (
      <li>
        <Link className={this.isActive(route) ? "is-active" : ""} to={to}>
          {label}
        </Link>
        {children}
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

export default SubNavigation;
