import React, { FC, ReactElement, useEffect, useState } from "react";
import { Button } from "../buttons";
import styled from "styled-components";

type Props = {
  label: string;
  children: ReactElement[];
  collapsed?: boolean;
  onCollapse?: (newStatus: boolean) => void;
  scrollTransitionAt?: number;
};

type CollapsedProps = {
  collapsed: boolean;
};

type PositionProps = CollapsedProps & {
  scrollPositionY: number;
  scrollTransitionAt: number;
};

const SectionContainer = styled.div<PositionProps>`
  position: ${props =>
    props.scrollPositionY > props.scrollTransitionAt && window.innerWidth > 770 ? "fixed" : "inherit"};
  top: ${props => props.scrollPositionY > props.scrollTransitionAt && window.innerWidth > 770 && "2rem"};
  width: ${props => (props.collapsed ? "5.5rem" : "20.5rem")};
`;

const SmallButton = styled(Button)<CollapsedProps>`
  padding-left: 1rem;
  padding-right: 1rem;
  margin-right: ${(props: CollapsedProps) => (props.collapsed ? "0" : "0.5rem")};
  height: 1.5rem;
`;

const MenuLabel = styled.p<CollapsedProps>`
  min-height: 2.5rem;
  display: flex;
  justify-content: ${props => (props.collapsed ? "center" : "left")};
`;

const Section: FC<Props> = ({ label, children, collapsed, onCollapse, scrollTransitionAt }) => {
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
    <SectionContainer
      collapsed={collapsed ? collapsed : false}
      scrollPositionY={onCollapse ? scrollPositionY : 0}
      scrollTransitionAt={scrollTransitionAt ? scrollTransitionAt : 250}
    >
      <MenuLabel className="menu-label" collapsed={collapsed ? collapsed : false}>
        {onCollapse && (
          <SmallButton
            color="info"
            className="is-medium"
            action={() => onCollapse(!collapsed)}
            collapsed={collapsed ? collapsed : false}
          >
            {arrowIcon}
          </SmallButton>
        )}
        {collapsed ? "" : label}
      </MenuLabel>
      <ul className="menu-list">{childrenWithProps}</ul>
    </SectionContainer>
  );
};

export default Section;
