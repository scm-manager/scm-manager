import React, { FC, useContext, MouseEvent } from "react";
import { ToastThemeContext, Themeable } from "./themes";
import styled from "styled-components";

type Props = {
  icon?: string;
  onClick?: () => void;
};

const ThemedButton = styled.button.attrs(props => ({
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

const ToastButton: FC<Props> = ({ icon, onClick, children }) => {
  const theme = useContext(ToastThemeContext);

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (onClick) {
      e.preventDefault();
      onClick();
    }
  };

  return (
    <ThemedButton theme={theme} onClick={handleClick}>
      {icon && <ToastButtonIcon className={`fas fa-fw fa-${icon}`} />} {children}
    </ThemedButton>
  );
};

export default ToastButton;
