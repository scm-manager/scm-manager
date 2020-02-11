import React, { FC, MouseEvent } from "react";
import styled from "styled-components";
import Tooltip from "../Tooltip";

const Button = styled.a`
  width: 50px;
  cursor: pointer;
  &:hover {
    color: #33b2e8;
  }
`;

type Props = {
  icon: string;
  tooltip: string;
  onClick: () => void;
};

const DiffButton: FC<Props> = ({ icon, tooltip, onClick }) => {
  const handleClick = (e: MouseEvent) => {
    e.preventDefault();
    onClick();
  };

  return (
    <Tooltip message={tooltip} location="top">
      <Button aria-label={tooltip} className="button" onClick={handleClick}>
        <i className={`fas fa-${icon}`} />
      </Button>
    </Tooltip>
  );
};

export default DiffButton;
