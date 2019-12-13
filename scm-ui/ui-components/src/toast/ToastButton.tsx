import React, { FC, useContext } from "react";
import { ToastThemeContext, Themeable } from "./themes";
import styled from "styled-components";

type Props = {
  icon?: string;
};

const ThemedButton = styled.div.attrs(props => ({
  className: "button"
}))<Themeable>`
  color: ${props => props.theme.primary};
  border-color: ${props => props.theme.primary};
  background-color: ${props => props.theme.secondary};
  font-size: 0.75rem;

  &:hover {
    color: ${props => props.theme.primary};
    border-color: ${props => props.theme.tertiary};
    background-color: ${props => props.theme.tertiary};
  }
`;

const ToastButtonIcon = styled.i`
  margin-right: 0.25rem;
`;

const ToastButton: FC<Props> = ({ icon, children }) => {
  const theme = useContext(ToastThemeContext);
  return (
    <ThemedButton theme={theme}>
      {icon && <ToastButtonIcon className={`fas fa-fw fa-${icon}`} />} {children}
    </ThemedButton>
  );
};

export default ToastButton;
