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

import * as React from "react";
import * as TestRenderer from "react-test-renderer";
import { LoginInfo as LoginInfoType } from "@scm-manager/ui-types";

import "@scm-manager/ui-tests";
import LoginInfo from "./LoginInfo";
import { Binder, BinderContext, extensionPoints } from "@scm-manager/ui-extensions";

jest.mock("@scm-manager/ui-api", () => ({
  useLoginInfo: jest.fn(() => ({
    isLoading: false,
    data: {
      _links: {},
      feature: { title: "Test", summary: "Test" },
      plugin: { summary: "Test", title: "Test" },
    } as LoginInfoType,
  })),
  urls: {
    getPageFromMatch: jest.fn(() => 1),
    withContextPath: jest.fn((it) => it),
  },
}));

describe("LoginInfo", () => {
  const withBinder = (ui, binder) => <BinderContext.Provider value={binder}>{ui}</BinderContext.Provider>;

  it("should render login page", () => {
    const loginHandler = jest.fn();
    const binder = new Binder("test");
    const reactTestRenderer = TestRenderer.create(withBinder(<LoginInfo loginHandler={loginHandler} />, binder));
    expect(reactTestRenderer.toJSON()).toMatchSnapshot();
  });

  it("should render extension", () => {
    const loginHandler = jest.fn();
    const binder = new Binder("test");
    binder.bind<extensionPoints.LoginForm>("login.form", () => <button>Login with OAuth2</button>);
    const reactTestRenderer = TestRenderer.create(withBinder(<LoginInfo loginHandler={loginHandler} />, binder));
    expect(reactTestRenderer.toJSON()).toMatchSnapshot();
  });

  it("should render extension with login form", () => {
    const loginHandler = jest.fn();
    const binder = new Binder("test");
    binder.bind<extensionPoints.LoginForm>("login.form", ({ children }) => (
      <>
        {children}
        <button>Login with OAuth2</button>
      </>
    ));
    const reactTestRenderer = TestRenderer.create(withBinder(<LoginInfo loginHandler={loginHandler} />, binder));
    expect(reactTestRenderer.toJSON()).toMatchSnapshot();
  });
});
