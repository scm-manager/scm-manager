//@flow
import React from "react";
import PrimaryNavigationLink from "./PrimaryNavigationLink";

type Props = {};

class PrimaryNavigation extends React.Component<Props> {
  render() {
    return (
      <nav className="tabs is-boxed">
        <ul>
          <PrimaryNavigationLink to="/users" label="Users" />
          <PrimaryNavigationLink to="/logout" label="Logout" />
        </ul>
      </nav>
    );
  }
}

export default PrimaryNavigation;
