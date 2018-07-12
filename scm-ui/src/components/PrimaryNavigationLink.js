//@flow
import * as React from "react";
import { Route, Link } from "react-router-dom";

type Props = {
  to: string,
  label: string,
  activeOnlyWhenExact?: boolean
};

class PrimaryNavigationLink extends React.Component<Props> {
  renderLink = (route: any) => {
    const { to, label } = this.props;
    return (
      <li className={route.match ? "is-active" : ""}>
        <Link to={to}>{label}</Link>
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
