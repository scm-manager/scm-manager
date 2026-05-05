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

// import { storiesOf } from "@storybook/react";
// import React, { ReactElement } from "react";
// import { MemoryRouter } from "react-router-dom";
// import styled from "styled-components";
// import { Binder, ExtensionPoint, BinderContext } from "@scm-manager/ui-extensions";
// import { SecondaryNavigationProvider } from "./SecondaryNavigationContext";
// import SecondaryNavigation from "./SecondaryNavigation";
// import SecondaryNavigationItem from "./SecondaryNavigationItem";
// import SubNavigation from "./SubNavigation";
//
// const Columns = styled.div`
//   margin: 2rem;
// `;
//
// const starships = (
//   <SubNavigation to="/hitchhiker/starships" label="Starships">
//     <SecondaryNavigationItem
//       to="/hitchhiker/starships/heart-of-gold"
//       icon="fas fa-star"
//       label="Heart Of Gold"
//       title="Heart Of Gold"
//     />
//     <SecondaryNavigationItem to="/hitchhiker/starships/titanic" label="Titanic" title="Starship Titanic" />
//   </SubNavigation>
// );
//
// const withRoute = (route: string) => {
//   return (story: ReactElement<any>) => <MemoryRouter initialEntries={[route]}>{story}</MemoryRouter>;
// };
//
// storiesOf("Secondary Navigation", module)
//   .addDecorator((story) => (
//     <Columns className="columns">
//       <div className="column is-3">
//         <SecondaryNavigationProvider>{story()}</SecondaryNavigationProvider>
//       </div>
//     </Columns>
//   ))
//   .add("Default", () =>
//     withRoute("/")(
//       <SecondaryNavigation label="Hitchhiker">
//         <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
//         <SecondaryNavigationItem to="/heart-of-gold" icon="fas fa-star" label="Heart Of Gold" title="Heart Of Gold" />
//       </SecondaryNavigation>
//     )
//   )
//   .add("Sub Navigation", () =>
//     withRoute("/")(
//       <SecondaryNavigation label="Hitchhiker">
//         <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
//         {starships}
//       </SecondaryNavigation>
//     )
//   )
//   .add("Extension Point", () => {
//     const binder = new Binder("menu");
//     binder.bind("subnav.sample", starships);
//     return withRoute("/hitchhiker/starships/titanic")(
//       <BinderContext.Provider value={binder}>
//         <SecondaryNavigation label="Hitchhiker">
//           <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
//           <ExtensionPoint name="subnav.sample" renderAll={true} />
//         </SecondaryNavigation>
//       </BinderContext.Provider>
//     );
//   })
//   .add("Active when match", () =>
//     withRoute("/hog")(
//       <SecondaryNavigation label="Hitchhiker">
//         <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
//         {/*TODO: fix after useAcitveMatch works*/}
//         <SecondaryNavigationItem
//           // activeWhenMatch={(route) => route.location?.pathname === "/hog"}
//           to="/heart-of-gold"
//           icon="fas fa-star"
//           label="Heart Of Gold"
//           title="Heart Of Gold"
//         />
//       </SecondaryNavigation>
//     )
//   );

import React from "react";
import { MemoryRouter } from "react-router-dom";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";
import { Binder, ExtensionPoint, BinderContext } from "@scm-manager/ui-extensions";

import { SecondaryNavigationProvider } from "./SecondaryNavigationContext";
import SecondaryNavigation from "./SecondaryNavigation";
import SecondaryNavigationItem from "./SecondaryNavigationItem";
import SubNavigation from "./SubNavigation";

// --- Helfer-Komponenten und Mock-Daten (aus der Original-Story übernommen) ---

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

// --- Storybook Metadaten ---

const meta: Meta<typeof SecondaryNavigation> = {
  title: "Navigation/SecondaryNavigation",
  component: SecondaryNavigation,
  // Dieser Decorator wendet das Layout und den notwendigen Provider auf alle Stories an.
  decorators: [
    (Story) => <MemoryRouter initialEntries={["/"]}>{Story()}</MemoryRouter>,
    (Story) => (
      <Columns className="columns">
        <div className="column is-3">
          <SecondaryNavigationProvider>
            <Story />
          </SecondaryNavigationProvider>
        </div>
      </Columns>
    ),
  ],
  tags: ["autodocs"],
};

export default meta;

// --- Story-Definitionen ---

type Story = StoryObj<typeof meta>;

// Da jede Story eine andere initiale Route für den MemoryRouter benötigt,
// verwenden wir für jede Story eine `render`-Funktion.

export const Default: Story = {
  render: () => (
    <SecondaryNavigation label="Hitchhiker">
      <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
      <SecondaryNavigationItem to="/heart-of-gold" icon="fas fa-star" label="Heart Of Gold" title="Heart Of Gold" />
    </SecondaryNavigation>
  ),
};

export const WithSubNavigation: Story = {
  name: "With Sub-Navigation",
  render: () => (
    <SecondaryNavigation label="Hitchhiker">
      <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
      {starships}
    </SecondaryNavigation>
  ),
};

export const WithExtensionPoint: Story = {
  name: "With Extension Point",
  render: () => {
    const binder = new Binder("menu");
    binder.bind("subnav.sample", starships);
    return (
      <BinderContext.Provider value={binder}>
        <SecondaryNavigation label="Hitchhiker">
          <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
          <ExtensionPoint name="subnav.sample" renderAll={true} />
        </SecondaryNavigation>
      </BinderContext.Provider>
    );
  },
};

export const ActiveWhenMatch: Story = {
  name: "Active When Match",
  render: () => (
    <SecondaryNavigation label="Hitchhiker">
      <SecondaryNavigationItem to="/42" icon="fas fa-puzzle-piece" label="Puzzle 42" title="Puzzle 42" />
      {/*TODO: fix after useAcitveMatch works*/}
      <SecondaryNavigationItem
        // activeWhenMatch={(route) => route.location?.pathname === "/hog"}
        to="/heart-of-gold"
        icon="fas fa-star"
        label="Heart Of Gold"
        title="Heart Of Gold"
      />
    </SecondaryNavigation>
  ),
};
