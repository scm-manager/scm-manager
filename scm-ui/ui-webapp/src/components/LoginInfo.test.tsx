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
