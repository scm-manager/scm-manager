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

import UserForm from "./UserForm";
import { I18nextProvider } from "react-i18next";
import i18nTest from "../../i18n.mock";
import { User } from "@scm-manager/ui-types";

const renderWithI18n = (component) => render(<I18nextProvider i18n={i18nTest}>{component}</I18nextProvider>);

describe("for user creation", () => {
  const fillForm = (userId: string, displayName: string, password: string, confirmation: string) => {
    fireEvent.change(screen.getByTestId("input-username"), {
      target: { value: userId },
    });

    fireEvent.change(screen.getByTestId("input-displayname"), {
      target: { value: displayName },
    });

    fireEvent.change(screen.getByTestId("input-password"), {
      target: { value: password },
    });

    fireEvent.change(screen.getByTestId("input-password-confirmation"), {
      target: { value: confirmation },
    });
  };

  it("should allow to create user", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm submitForm={mockSubmitForm} />);

    fillForm("trillian", "Tricia McMillan", "password", "password");

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).toBeCalled();
  });

  it("should prevent to submit empty form", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm submitForm={mockSubmitForm} />);

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });

  it("should prevent to submit form without user id", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm submitForm={mockSubmitForm} />);

    fillForm("", "Arthur Dent", "password", "password");

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });

  it("should prevent to submit form without display name", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm submitForm={mockSubmitForm} />);

    fillForm("trillian", "", "password", "password");

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });

  it("should prevent to submit form without password", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm submitForm={mockSubmitForm} />);

    fillForm("trillian", "Tricia McMillan", "", "");

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });

  it("should prevent to submit form with wrong password confirmation", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm submitForm={mockSubmitForm} />);

    fillForm("trillian", "Tricia McMillan", "password", "different");

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });
});

describe("for user edit", () => {
  const user: User = {
    name: "trillian",
    mail: "tricia@hog.space",
    displayName: "Tricia McMillan",
    password: undefined,
    active: true,
    external: false,
    _links: {},
  };

  it("should allow to edit user with changed display name", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm user={user} submitForm={mockSubmitForm} />);

    fireEvent.change(screen.getByTestId("input-displayname"), {
      target: { value: "Just Tricia" },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).toBeCalled();
  });

  it("should allow to edit user with changed email", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm user={user} submitForm={mockSubmitForm} />);

    fireEvent.change(screen.getByTestId("input-mail"), {
      target: { value: "tricia@hg2g.com" },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).toBeCalled();
  });

  it("should allow to edit user with changed active flag", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm user={user} submitForm={mockSubmitForm} />);

    fireEvent.click(screen.getByTestId("checkbox-active"));

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).toBeCalled();
  });

  it("should prevent to submit unchanged user", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm user={user} submitForm={mockSubmitForm} />);

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });

  it("should prevent to edit user with incorrect email", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm user={user} submitForm={mockSubmitForm} />);

    fireEvent.change(screen.getByTestId("input-mail"), {
      target: { value: "do_not_reply" },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });

  it("should prevent to edit user with empty display name", () => {
    const mockSubmitForm = jest.fn();

    renderWithI18n(<UserForm user={user} submitForm={mockSubmitForm} />);

    fireEvent.change(screen.getByTestId("input-displayname"), {
      target: { value: "" },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSubmitForm).not.toBeCalled();
  });
});
