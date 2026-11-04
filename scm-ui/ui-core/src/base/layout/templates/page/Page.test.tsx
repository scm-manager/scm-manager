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

import React, { ComponentProps, createRef } from "react";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Page from "./Page";

vi.mock("react-i18next", () => {
  const t = (key: string) => key;
  const i18n = { language: "en", languages: ["en"] };
  // supports both `const [t] = useTranslation()` and `const { t } = useTranslation()`
  return { useTranslation: () => Object.assign([t, i18n], { t, i18n }) };
});

const renderPage = (props: Partial<ComponentProps<typeof Page>> = {}) =>
  render(
    <MemoryRouter>
      <Page title="Repositories" subtitle="Overview of all repositories" {...props}>
        <p>Content</p>
      </Page>
    </MemoryRouter>,
  );

describe("Page", () => {
  it("should render title, subtitle and content", () => {
    renderPage();

    expect(screen.getByRole("heading", { level: 1, name: "Repositories" })).toBeTruthy();
    expect(screen.getByRole("heading", { level: 2, name: "Overview of all repositories" })).toBeTruthy();
    expect(screen.getByText("Content")).toBeTruthy();
  });

  it("should render content after the title", () => {
    renderPage({ afterTitle: <span>Switch</span> });

    const title = screen.getByRole("heading", { level: 1 });
    expect(title.nextElementSibling?.textContent).toBe("Switch");
  });

  it("should render no header without title and subtitle", () => {
    renderPage({ title: undefined, subtitle: undefined, actions: <button>Create</button> });

    expect(screen.queryByRole("heading")).toBeNull();
    expect(screen.queryByRole("button", { name: "Create" })).toBeNull();
    expect(screen.getByText("Content")).toBeTruthy();
  });

  it("should render actions with a separator", () => {
    const { container } = renderPage({ actions: <button>Create</button> });

    expect(screen.getByRole("button", { name: "Create" })).toBeTruthy();
    expect(container.querySelector("hr.header-with-actions")).toBeTruthy();
  });

  it("should render no separator without actions", () => {
    const { container } = renderPage();

    expect(container.querySelector("hr.header-with-actions")).toBeNull();
  });

  it("should hide content and actions on error", () => {
    const { container } = renderPage({ actions: <button>Create</button>, error: new Error("Something failed") });

    expect(screen.getByText("Something failed")).toBeTruthy();
    expect(screen.queryByText("Content")).toBeNull();
    expect(screen.queryByRole("button", { name: "Create" })).toBeNull();
    expect(container.querySelector("hr.header-with-actions")).toBeNull();
    expect(screen.getByRole("heading", { level: 1, name: "Repositories" })).toBeTruthy();
  });

  it("should keep the content on error if requested", () => {
    renderPage({ error: new Error("Something failed"), showContentOnError: true });

    expect(screen.getByText("Something failed")).toBeTruthy();
    expect(screen.getByText("Content")).toBeTruthy();
  });

  it("should render a loading indicator instead of the content", () => {
    renderPage({ loading: true });

    expect(screen.getByAltText("loading.alt")).toBeTruthy();
    expect(screen.queryByText("Content")).toBeNull();
    expect(screen.getByRole("heading", { level: 1, name: "Repositories" })).toBeTruthy();
  });

  it("should pass ref and attributes to the section", () => {
    const ref = createRef<HTMLElement>();
    renderPage({ ref, className: "custom", "aria-label": "Page" });

    expect(ref.current).toBe(screen.getByRole("region", { name: "Page" }));
    expect(ref.current?.className).toBe("section custom");
  });
});
