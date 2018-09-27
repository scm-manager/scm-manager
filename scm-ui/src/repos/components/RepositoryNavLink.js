//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  to: string,
  label: string,
  linkName: string
};

/**
 * Component renders only if the repository contains the link with the given name.
 */
class RepositoryNavLink extends React.Component<Props> {
  render() {
    const { linkName, to, label, repository } = this.props;

    if (!repository._links[linkName]) {
      return null;
    }

    return <NavLink to={to} label={label} />;
  }
}

export default RepositoryNavLink;
