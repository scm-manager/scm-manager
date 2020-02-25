import React, { FC, ReactNode } from "react";
import Icon from "../Icon";
import { Button } from "../buttons";
import styled from "styled-components";

type Props = {
  label: string;
  children?: ReactNode;
  collapsed?: boolean;
  onCollapse?: (newStatus: boolean) => void;
};

const SmallButton = styled(Button)`
  height: 1.5rem;
  width: 1rem;
  position: absolute;
  right: 1.5rem;
  > {
    outline: none;
  }
`;

const MenuLabel = styled.p`
  min-height: 2.5rem;
`;

const Section: FC<Props> = ({ label, children, collapsed, onCollapse }) => {
  const childrenWithProps = React.Children.map(children, child => React.cloneElement(child, { collapsed: collapsed }));
  return (
    <div>
      <MenuLabel className="menu-label">
        {collapsed ? "" : label}
        {onCollapse && (
          <SmallButton color="info" className="is-small" action={onCollapse}>
            <Icon name={collapsed ? "arrow-left" : "arrow-right"} color="white" />
          </SmallButton>
        )}
      </MenuLabel>
      <ul className="menu-list">{childrenWithProps}</ul>
    </div>
  );
};

export default Section;
