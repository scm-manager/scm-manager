import React, { FC, MouseEvent } from "react";
import styled from "styled-components";

const Button = styled.a`
  width: 50px;
  cursor: pointer;
  &:hover {
    color: #33b2e8;
  }
`;

type Props = {
  icon: string;
  title: string;
  onClick: () => void;
};

const DiffButton: FC<Props> = ({ icon, title, onClick }) => {
  const handleClick = (e: MouseEvent) => {
    e.preventDefault();
    onClick();
  };

  return (
    <Button title={title} className="button" onClick={handleClick}>
      <i className={`fas fa-${icon}`} />
    </Button>
  );
};

export default DiffButton;
