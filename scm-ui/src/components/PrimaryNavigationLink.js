//@flow
import * as React from "react";
import { Route, Link } from "react-router-dom";

type Props = {
  to: string,
  activeOnlyWhenExact?: boolean,
  children?: React.Node
};

class PrimaryNavigationLink extends React.Component<Props> {
  renderLink = (route: any) => {
    const { to, children } = this.props;
    return (
      <li className={route.match ? "is-active" : ""}>
        <Link to={to}>{children}</Link>
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

export default PrimaryNavigationLink;
