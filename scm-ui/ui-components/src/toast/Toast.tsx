import React, { FC } from "react";
import { createPortal } from "react-dom";
import styled from "styled-components";
import { getTheme, Themeable, ToastThemeContext, Type } from "./themes";
import usePortalRootElement from "../usePortalRootElement";

type Props = {
  type: Type;
  title: string;
};

const Container = styled.div<Themeable>`
  z-index: 99999;
  position: fixed;
  padding: 1.5rem;
  right: 1.5rem;
  bottom: 1.5rem;
  color: ${props => props.theme.primary};
  background-color: ${props => props.theme.secondary};
  max-width: 18rem;
  font-size: 0.75rem;
  border-radius: 5px;
  animation: 0.5s slide-up;

  & > p {
    margin-bottom: 0.5rem;
  }

  @keyframes slide-up {
    from {
      bottom: -10rem;
    }
    to {
      bottom: 1.5rem;
    }
  }
`;

const Title = styled.h1<Themeable>`
  margin-bottom: 0.25rem;
  font-weight: bold;
`;

const Toast: FC<Props> = ({ children, title, type }) => {
  const rootElement = usePortalRootElement("toastRoot");
  if (!rootElement) {
    // portal not yet ready
    return null;
  }

  const theme = getTheme(type);
  const content = (
    <Container theme={theme}>
      <Title theme={theme}>{title}</Title>
      <ToastThemeContext.Provider value={theme}>{children}</ToastThemeContext.Provider>
    </Container>
  );

  return createPortal(content, rootElement);
};

export default Toast;
