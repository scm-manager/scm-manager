import React, { FC, ReactNode, useEffect, useState } from "react";
import { Button } from "../buttons";
import styled from "styled-components";

type Props = {
  label: string;
  children?: ReactNode;
  collapsed?: boolean;
  onCollapse?: (newStatus: boolean) => void;
};

const SectionContainer = styled.div`
  position: ${props => (props.scrollPositionY > 210 ? "fixed" : "absolute")};
  top: ${props => props.scrollPositionY > 210 && "4.5rem"};
  width: ${props => (props.collapsed ? "5.5rem" : "20.5rem")};
`;

const SmallButton = styled(Button)`
  height: 1.5rem;
  width: 1rem;
  position: absolute;
  right: 1.5rem;
`;

const MenuLabel = styled.p`
  min-height: 2.5rem;
`;

const Section: FC<Props> = ({ label, children, collapsed, onCollapse }) => {
  const [scrollPositionY, setScrollPositionY] = useState(0);

  useEffect(() => {
    window.addEventListener("scroll", () => setScrollPositionY(window.pageYOffset));

    return () => {
      window.removeEventListener("scroll", () => setScrollPositionY(window.pageYOffset));
    };
  }, []);

  const childrenWithProps = React.Children.map(children, child => React.cloneElement(child, { collapsed: collapsed }));
  const arrowIcon = collapsed ? <i className="fas fa-caret-down" /> : <i className="fas fa-caret-right" />;

  return (
    <SectionContainer collapsed={collapsed} scrollPositionY={scrollPositionY}>
      <MenuLabel className="menu-label">
        {collapsed ? "" : label}
        {onCollapse && (
          <SmallButton color="info" className="is-small" action={onCollapse}>
            {arrowIcon}
          </SmallButton>
        )}
      </MenuLabel>
      <ul className="menu-list">{childrenWithProps}</ul>
    </SectionContainer>
  );
};

export default Section;
