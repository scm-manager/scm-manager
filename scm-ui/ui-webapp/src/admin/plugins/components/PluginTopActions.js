// @flow
import * as React from "react";
import classNames from "classnames";
import styled from "styled-components";

type Props = {
  children?: React.Node
};

const ChildWrapper = styled.div`
  justify-content: flex-end;
  align-items: center;
`;

export default class PluginTopActions extends React.Component<Props> {
  render() {
    const { children } = this.props;
    return (
      <ChildWrapper
        className={classNames(
          "column",
          "is-flex",
          "is-one-fifths",
          "is-mobile-action-spacing"
        )}
      >
        {children}
      </ChildWrapper>
    );
  }
}
