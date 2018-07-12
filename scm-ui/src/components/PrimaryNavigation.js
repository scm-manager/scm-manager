//@flow
import React from "react";
import PrimaryNavigationLink from "./PrimaryNavigationLink";
import PrimaryNavigationAction from "./PrimaryNavigationAction";

type Props = {
  onLogout: () => void
};

class PrimaryNavigation extends React.Component<Props> {
  render() {
    return (
      <nav className="tabs is-boxed">
        <ul>
          <PrimaryNavigationLink to="/users" label="Users" />
          <PrimaryNavigationAction
            onClick={this.props.onLogout}
            label="Logout"
          />
        </ul>
      </nav>
    );
  }
}

export default PrimaryNavigation;
