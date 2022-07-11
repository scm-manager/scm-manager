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
import { fireEvent, render, screen } from "@testing-library/react";
import { hri } from "human-readable-ids";

import UserForm from "./UserForm";
import { I18nextProvider } from "react-i18next";
import i18nTest from "../../i18n.mock";
import { User } from "@scm-manager/ui-types";

const renderWithI18n = (component) => render(<I18nextProvider i18n={i18nTest}>{component}</I18nextProvider>);

describe("create", () => {
  it("should allow to create user", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm submitForm={mockSubmitForm} />);

    const newUser = hri.random();
    const password = hri.random();

    fireEvent.change(screen.getByTestId("input-username"), {
      target: { value: newUser },
    });

    fireEvent.change(screen.getByTestId("input-displayname"), {
      target: { value: newUser },
    });

    fireEvent.change(screen.getByTestId("input-password"), {
      target: { value: password },
    });

    fireEvent.change(screen.getByTestId("input-password-confirmation"), {
      target: { value: password },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).toBeCalled();
  });

  it("should allow to edit user", () => {
    const mockSubmitForm = jest.fn();

    const oldUser = hri.random();

    const user: User = {
      name: oldUser,
      mail: oldUser,
      displayName: oldUser,
      password: undefined,
      active: true,
      external: false,
      _links: {},
    };

    renderWithI18n(<UserForm user={user} submitForm={mockSubmitForm} />);

    const newUser = hri.random();

    fireEvent.change(screen.getByTestId("input-displayname"), {
      target: { value: newUser },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).toBeCalled();
  });
});
