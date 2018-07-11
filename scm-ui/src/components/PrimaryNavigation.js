//@flow
import React from "react";
import PrimaryNavigationLink from "./PrimaryNavigationLink";

class PrimaryNavigation extends React.Component<Props> {
  render() {
    return (
      <nav className="tabs is-boxed">
        <ul>
          <PrimaryNavigationLink to="/users">Users</PrimaryNavigationLink>
        </ul>
      </nav>
    );
  }
}

export default PrimaryNavigation;
