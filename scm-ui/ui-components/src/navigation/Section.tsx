import React, { FC, ReactElement, useEffect, useState } from "react";
import { Button } from "../buttons";
import styled from "styled-components";

type Props = {
  label: string;
  children: ReactElement[];
  collapsed?: boolean;
  onCollapse?: (newStatus: boolean) => void;
};

type StylingProps = {
  scrollPositionY: number;
  collapsed: boolean;
};

const SectionContainer = styled.div`
  position: ${(props: StylingProps) => (props.scrollPositionY > 210 && window.innerWidth > 770 ? "fixed" : "inherit")};
  top: ${(props: StylingProps) => props.scrollPositionY > 210 && window.innerWidth > 770 && "4.5rem"};
  width: ${(props: StylingProps) => (props.collapsed ? "5.5rem" : "20.5rem")};
`;

const SmallButton = styled(Button)`
  height: 1.5rem;
`;

const MenuLabel = styled.p`
  min-height: 2.5rem;
  display: flex;
  justify-content: ${(props: { collapsed: boolean }) => (props.collapsed ? "center" : "space-between")};
`;

const Section: FC<Props> = ({ label, children, collapsed, onCollapse }) => {
  const [scrollPositionY, setScrollPositionY] = useState(0);

  useEffect(() => {
    window.addEventListener("scroll", () => setScrollPositionY(window.pageYOffset));

    return () => {
      window.removeEventListener("scroll", () => setScrollPositionY(window.pageYOffset));
    };
  }, []);

  const childrenWithProps = React.Children.map(children, (child: ReactElement) =>
    React.cloneElement(child, { collapsed: collapsed })
  );
  const arrowIcon = collapsed ? <i className="fas fa-caret-down" /> : <i className="fas fa-caret-right" />;

  return (
    <SectionContainer collapsed={collapsed ? collapsed : false} scrollPositionY={onCollapse ? scrollPositionY : 0}>
      <MenuLabel className="menu-label" collapsed={collapsed ? collapsed : false}>
        {collapsed ? "" : label}
        {onCollapse && (
          <SmallButton color="info" className="is-medium" action={() => onCollapse(!collapsed)}>
            {arrowIcon}
          </SmallButton>
        )}
      </MenuLabel>
      <ul className="menu-list">{childrenWithProps}</ul>
    </SectionContainer>
  );
};

export default Section;
