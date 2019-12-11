import React, { FC } from "react";
import styled from "styled-components";

const Buttons = styled.div`
  display: flex;
  padding-top: 0.5rem;
  width: 100%;

  & > * {
    flex-grow: 1;
  }

  & > *:not(:last-child) {
    margin-right: 0.5rem;
  }
`;

const ToastButtons: FC = ({ children }) => <Buttons>{children}</Buttons>;

export default ToastButtons;
