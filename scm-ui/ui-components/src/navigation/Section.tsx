import React, { FC, ReactElement, ReactNode, useEffect, useState } from "react";
import { Button } from "../buttons";
import styled from "styled-components";
import SubNavigation from "./SubNavigation";
import { useLocation, matchPath } from "react-router-dom";

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
  display: flex;
  align-items: center;
`;

const MenuLabel = styled.p<CollapsedProps>`
  min-height: 2.5rem;
  display: flex;
  justify-content: ${props => (props.collapsed ? "center" : "left")};
`;

const createParentPath = (to: string) => {
  const parents = to.split("/");
  parents.splice(-1, 1);
  return parents.join("/");
};

const isSubNavigationActive = (children: ReactNode, url: string): boolean => {
  const childArray = React.Children.toArray(children);
  const match = childArray
    .filter(child => {
      // what about extension points?
      // @ts-ignore
      return child.type.name === SubNavigation.name;
    })
    .map(child => {
      // @ts-ignore
      return child.props;
    })
    .find(props => {
      const path = createParentPath(props.to);
      const matches = matchPath(url, {
        path,
        exact: props.activeOnlyWhenExact as boolean
      });
      return matches != null;
    });

  return match != null;
};

const Section: FC<Props> = ({ label, children, collapsed = false, onCollapse, scrollTransitionAt }) => {
  const [scrollPositionY, setScrollPositionY] = useState(0);
  const location = useLocation();

  useEffect(() => {
    window.addEventListener("scroll", () => setScrollPositionY(window.pageYOffset));

    return () => {
      window.removeEventListener("scroll", () => setScrollPositionY(window.pageYOffset));
    };
  }, []);

  const subNavActive = isSubNavigationActive(children, location.pathname);
  const isCollapsed = collapsed && !subNavActive;

  const childrenWithProps = React.Children.map(children, (child: ReactElement) =>
    React.cloneElement(child, { collapsed: isCollapsed })
  );
  const arrowIcon = isCollapsed ? <i className="fas fa-caret-down" /> : <i className="fas fa-caret-right" />;

  return (
    <SectionContainer
      collapsed={isCollapsed}
      scrollPositionY={onCollapse ? scrollPositionY : 0}
      scrollTransitionAt={scrollTransitionAt ? scrollTransitionAt : 250}
    >
      <MenuLabel className="menu-label" collapsed={isCollapsed}>
        {onCollapse && !subNavActive && (
          <SmallButton color="info" className="is-medium" action={() => onCollapse(!isCollapsed)} collapsed={isCollapsed}>
            {arrowIcon}
          </SmallButton>
        )}
        {isCollapsed ? "" : label}
      </MenuLabel>
      <ul className="menu-list">{childrenWithProps}</ul>
    </SectionContainer>
  );
};

export default Section;
