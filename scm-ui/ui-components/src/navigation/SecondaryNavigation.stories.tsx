/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { storiesOf } from "@storybook/react";
import React, { ReactElement } from "react";
import SecondaryNavigation from "./SecondaryNavigation";
import SecondaryNavigationItem from "./SecondaryNavigationItem";
import styled from "styled-components";
import SubNavigation from "./SubNavigation";
import { Binder, ExtensionPoint, BinderContext } from "@scm-manager/ui-extensions";
import { MemoryRouter } from "react-router-dom";

const Columns = styled.div`
  margin: 2rem;
`;

const starships = (
  <SubNavigation to="/hitchhiker/starships" label="Starships">
    <SecondaryNavigationItem
      to="/hitchhiker/starships/heart-of-gold"
      icon="fas fa-star"
      label="Heart Of Gold"
      title="Heart Of Gold"
    />
    <SecondaryNavigationItem to="/hitchhiker/starships/titanic" label="Titanic" title="Starship Titanic" />
  </SubNavigation>
);

const withRoute = (route: string) => {
  return (story: ReactElement<any>) => <MemoryRouter initialEntries={[route]}>{story}</MemoryRouter>;
};

storiesOf("Secondary Navigation", module)
  .addDecorator((story) => (
    <Columns className="columns">
      <div className="column is-3">{story()}</div>
    </Columns>
  ))
  .add("Default", () =>
    withRoute("/")(
      <SecondaryNavigation label="Hitchhiker">
        <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
        <SecondaryNavigationItem to="/heart-of-gold" icon="fas fa-star" label="Heart Of Gold" title="Heart Of Gold" />
      </SecondaryNavigation>
    )
  )
  .add("Sub Navigation", () =>
    withRoute("/")(
      <SecondaryNavigation label="Hitchhiker">
        <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
        {starships}
      </SecondaryNavigation>
    )
  )
  .add("Extension Point", () => {
    const binder = new Binder("menu");
    binder.bind("subnav.sample", starships);
    return withRoute("/hitchhiker/starships/titanic")(
      <BinderContext.Provider value={binder}>
        <SecondaryNavigation label="Hitchhiker">
          <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
          <ExtensionPoint name="subnav.sample" renderAll={true} />
        </SecondaryNavigation>
      </BinderContext.Provider>
    );
  })
  .add("Active when match", () =>
    withRoute("/hog")(
      <SecondaryNavigation label="Hitchhiker">
        <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
        <SecondaryNavigationItem
          activeWhenMatch={(route) => route.location?.pathname === "/hog"}
          to="/heart-of-gold"
          icon="fas fa-star"
          label="Heart Of Gold"
          title="Heart Of Gold"
        />
      </SecondaryNavigation>
    )
  );
