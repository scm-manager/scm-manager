//@flow
import React from "react";
import styled from "styled-components";
import Button, { type ButtonProps } from "./Button";

const Wrapper = styled.div`
  margin-top: 2em;
  padding: 1em 1em;
  border: 2px solid #e9f7fd;
`;

export default class CreateButton extends React.Component<ButtonProps> {
  render() {
    return (
      <Wrapper className="has-text-centered">
        <Button color="primary" {...this.props} />
      </Wrapper>
    );
  }
}
