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

// import React from "react";
// import { storiesOf } from "@storybook/react";
// import Footer from "./Footer";
// import { Binder, BinderContext, extensionPoints } from "@scm-manager/ui-extensions";
// import { Me } from "@scm-manager/ui-types";
// import { EXTENSION_POINT } from "../avatar/Avatar";
// // @ts-ignore ignore unknown png
// import hitchhiker from "../__resources__/hitchhiker.png";
// // @ts-ignore ignore unknown jpg
// import marvin from "../__resources__/marvin.jpg";
// import NavLink from "../navigation/NavLink";
// import ExternalNavLink from "../navigation/ExternalNavLink";
// import { MemoryRouter } from "react-router-dom";
//
// const trillian: Me = {
//   name: "trillian",
//   displayName: "Trillian McMillian",
//   mail: "tricia@hitchhiker.com",
//   groups: ["crew"],
//   _links: {},
// };
//
// const bindAvatar = (binder: Binder, avatar: string) =>
//   binder.bind<extensionPoints.AvatarFactory>(EXTENSION_POINT, () => avatar);
//
// const bindLinks = (binder: Binder) => {
//   binder.bind("footer.information", () => <ExternalNavLink to="#" label="REST API" />);
//   binder.bind("footer.information", () => <ExternalNavLink to="#" label="CLI" />);
//   binder.bind("footer.support", () => <ExternalNavLink to="#" label="FAQ" />);
//   binder.bind("profile.setting", () => <NavLink label="Authorized Keys" to="#" />);
// };
//
// const withBinder = (binder: Binder) => {
//   return (
//     <BinderContext.Provider value={binder}>
//       <Footer me={trillian} version="2.0.0" links={{}} />
//     </BinderContext.Provider>
//   );
// };
//
// storiesOf("Footer", module)
//   .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
//   .add("Default", () => {
//     return <Footer me={trillian} version="2.0.0" links={{}} />;
//   })
//   .add("With Avatar", () => {
//     const binder = new Binder("avatar-story");
//     bindAvatar(binder, hitchhiker);
//     return withBinder(binder);
//   })
//   .add("With Plugin Links", () => {
//     const binder = new Binder("link-story");
//     bindLinks(binder);
//     return withBinder(binder);
//   })
//   .add("Full", () => {
//     const binder = new Binder("link-story");
//     bindAvatar(binder, marvin);
//     bindLinks(binder);
//     return withBinder(binder);
//   });

import React from "react";
import type { Meta, StoryObj } from "@storybook/react";
import { Binder, BinderContext, extensionPoints } from "@scm-manager/ui-extensions";
import { Me } from "@scm-manager/ui-types";

import Footer from "./Footer";
import NavLink from "../navigation/NavLink";
import ExternalNavLink from "../navigation/ExternalNavLink";
import { EXTENSION_POINT } from "../avatar/Avatar";
// @ts-ignore ignore unknown png
import hitchhiker from "../__resources__/hitchhiker.png";
// @ts-ignore ignore unknown jpg
import marvin from "../__resources__/marvin.jpg";
import { MemoryRouter } from "react-router-dom";

// --- Helfer-Funktionen und Mock-Daten (aus der Original-Story übernommen) ---

const trillian: Me = {
  name: "trillian",
  displayName: "Trillian McMillian",
  mail: "tricia@hitchhiker.com",
  groups: ["crew"],
  _links: {},
};

const bindAvatar = (binder: Binder, avatar: string) =>
  binder.bind<extensionPoints.AvatarFactory>(EXTENSION_POINT, () => avatar);

const bindLinks = (binder: Binder) => {
  binder.bind("footer.information", () => <ExternalNavLink to="#" label="REST API" />);
  binder.bind("footer.information", () => <ExternalNavLink to="#" label="CLI" />);
  binder.bind("footer.support", () => <ExternalNavLink to="#" label="FAQ" />);
  binder.bind("profile.setting", () => <NavLink label="Authorized Keys" to="#" />);
};

const withBinder = (binder: Binder) => (
  <BinderContext.Provider value={binder}>
    <Footer me={trillian} version="2.0.0" links={{}} />
  </BinderContext.Provider>
);

// --- Storybook Metadaten ---

const meta: Meta<typeof Footer> = {
  title: "Layout/Footer",
  component: Footer,
  tags: ["autodocs"],
  decorators: [(Story) => <MemoryRouter initialEntries={["/"]}>{Story()}</MemoryRouter>],
};

export default meta;

// --- Story-Definitionen ---

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    me: trillian,
    version: "2.0.0",
    links: {},
  },
};

// Stories, die den BinderContext verwenden, nutzen eine `render`-Funktion
export const WithAvatar: Story = {
  render: () => {
    const binder = new Binder("avatar-story");
    bindAvatar(binder, hitchhiker);
    return withBinder(binder);
  },
};

export const WithPluginLinks: Story = {
  name: "With Plugin Links",
  render: () => {
    const binder = new Binder("link-story");
    bindLinks(binder);
    return withBinder(binder);
  },
};

export const Full: Story = {
  name: "With Avatar and Plugin Links",
  render: () => {
    const binder = new Binder("full-story");
    bindAvatar(binder, marvin);
    bindLinks(binder);
    return withBinder(binder);
  },
};
