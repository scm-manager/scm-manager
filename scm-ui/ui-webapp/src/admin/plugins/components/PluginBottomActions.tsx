import * as React from "react";
import styled from "styled-components";

type Props = {
  children?: React.Node;
};

const ActionWrapper = styled.div`
  justify-content: center;
  margin-top: 2em;
  padding: 1em 1em;
  border: 2px solid #e9f7df;
`;

export default class PluginBottomActions extends React.Component<Props> {
  render() {
    const { children } = this.props;
    return <ActionWrapper className="is-flex">{children}</ActionWrapper>;
  }
}
