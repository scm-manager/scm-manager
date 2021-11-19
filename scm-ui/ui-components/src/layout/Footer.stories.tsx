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
import React from "react";
import { storiesOf } from "@storybook/react";
import Footer from "./Footer";
import { Binder, BinderContext, extensionPoints } from "@scm-manager/ui-extensions";
import { Me } from "@scm-manager/ui-types";
import { EXTENSION_POINT } from "../avatar/Avatar";
// @ts-ignore ignore unknown png
import hitchhiker from "../__resources__/hitchhiker.png";
// @ts-ignore ignore unknown jpg
import marvin from "../__resources__/marvin.jpg";
import NavLink from "../navigation/NavLink";
import ExternalNavLink from "../navigation/ExternalNavLink";
import { MemoryRouter } from "react-router-dom";

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

const withBinder = (binder: Binder) => {
  return (
    <BinderContext.Provider value={binder}>
      <Footer me={trillian} version="2.0.0" links={{}} />
    </BinderContext.Provider>
  );
};

storiesOf("Footer", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .add("Default", () => {
    return <Footer me={trillian} version="2.0.0" links={{}} />;
  })
  .add("With Avatar", () => {
    const binder = new Binder("avatar-story");
    bindAvatar(binder, hitchhiker);
    return withBinder(binder);
  })
  .add("With Plugin Links", () => {
    const binder = new Binder("link-story");
    bindLinks(binder);
    return withBinder(binder);
  })
  .add("Full", () => {
    const binder = new Binder("link-story");
    bindAvatar(binder, marvin);
    bindLinks(binder);
    return withBinder(binder);
  });
