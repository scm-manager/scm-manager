/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

storiesOf("Navigation|Secondary", module)
  .addDecorator(story => (
    <Columns className="columns">
      <div className="column is-3">{story()}</div>
    </Columns>
  ))
  .add("Default", () =>
    withRoute("/")(
      <SecondaryNavigation label="Hitchhiker" collapsed={false} onCollapse={() => {}}>
        <SecondaryNavigationItem to="/" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
        <SecondaryNavigationItem to="/some" icon="fas fa-star" label="Heart Of Gold" title="Heart Of Gold" />
      </SecondaryNavigation>
    )
  )
  .add("Collapsed", () =>
    withRoute("/")(
      <SecondaryNavigation label="Hitchhiker" collapsed={true} onCollapse={() => {}}>
        <SecondaryNavigationItem to="/" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
        <SecondaryNavigationItem to="/some" icon="fas fa-star" label="Heart Of Gold" title="Heart Of Gold" />
      </SecondaryNavigation>
    )
  )
  .add("Sub Navigation", () =>
    withRoute("/")(
      <SecondaryNavigation label="Hitchhiker" collapsed={false} onCollapse={() => {}}>
        <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
        {starships}
      </SecondaryNavigation>
    )
  )
  .add("Sub Navigation Collapsed", () =>
    withRoute("/hitchhiker/starships/heart-of-gold")(
      <SecondaryNavigation label="Hitchhiker" collapsed={true} onCollapse={() => {}}>
        <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
        {starships}
      </SecondaryNavigation>
    )
  )
  .add("Collapsed EP Sub", () => {
    const binder = new Binder("menu");
    binder.bind("subnav.sample", starships);
    return withRoute("/hitchhiker/starships/titanic")(
      <BinderContext.Provider value={binder}>
        <SecondaryNavigation label="Hitchhiker" collapsed={true} onCollapse={() => {}}>
          <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
          <ExtensionPoint name="subnav.sample" renderAll={true} />
        </SecondaryNavigation>
      </BinderContext.Provider>
    );
  });
