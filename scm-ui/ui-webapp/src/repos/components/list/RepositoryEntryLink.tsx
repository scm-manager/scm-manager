import React from "react";
import { Link } from "react-router-dom";
import styled from "styled-components";
import { Icon } from "@scm-manager/ui-components";

type Props = {
  to: string;
  icon: string;
};

const PointerEventsLink = styled(Link)`
  pointer-events: all;
`;

class RepositoryEntryLink extends React.Component<Props> {
  render() {
    const { to, icon } = this.props;
    return (
      <PointerEventsLink className="level-item" to={to}>
        <Icon className="fa-lg" name={icon} color="inherit" />
      </PointerEventsLink>
    );
  }
}

export default RepositoryEntryLink;
