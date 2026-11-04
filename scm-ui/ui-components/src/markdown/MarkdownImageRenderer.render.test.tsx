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

import React from "react";
import { act, cleanup, render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import createWrapper from "../../../ui-api/src/tests/createWrapper";
import createInfiniteCachingClient from "../../../ui-api/src/tests/createInfiniteCachingClient";
import { apiClient, RepositoryRevisionContextProvider } from "@scm-manager/ui-api";
import { Repository } from "@scm-manager/ui-types";
import MarkdownImageRenderer from "./MarkdownImageRenderer";

// Use the source module so the hooks and provider share the same React Query context.
vi.mock("@scm-manager/ui-api", () => import("../../../ui-api/src"));

const repository: Repository = {
  namespace: "space",
  name: "ship",
  type: "git",
  _links: { content: { href: "/content/{revision}/{path}" } },
};

const renderImage = (path: string, src = "image.png") => {
  const client = createInfiniteCachingClient();
  client.setQueryData(["index"], { _links: { repositories: { href: "/repositories" } } });
  client.setQueryData(["repository", "space", "ship"], repository);
  const element = <MarkdownImageRenderer src={src} alt="Example" base="/" />;
  const router = createMemoryRouter(
    [
      { path: "/admin/config", element },
      { path: "/namespace/:namespace", element },
      { path: "/name/:name", element },
      { path: "/repo/:namespace/:name/*", element },
    ],
    { initialEntries: [path] },
  );
  const Wrapper = createWrapper(client);
  render(
    <Wrapper>
      <RepositoryRevisionContextProvider revision="main">
        <RouterProvider router={router} />
      </RepositoryRevisionContextProvider>
    </Wrapper>,
  );
  return router;
};

const expectImage = async (src: string) => {
  expect((await screen.findByRole("img", { name: "Example" })).getAttribute("src")).toBe(src);
};

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

it.each(["/admin/config", "/namespace/space", "/name/ship"])(
  "renders an image without requesting a repository on %s",
  async (path) => {
    const get = vi.spyOn(apiClient, "get");
    renderImage(path);
    await expectImage("image.png");
    expect(get).not.toHaveBeenCalled();
  },
);

it("resolves relative images from the cached repository", async () => {
  const get = vi.spyOn(apiClient, "get");
  renderImage("/repo/space/ship/code/sources/main/docs/README.md");
  await expectImage("/content/main/docs/image.png");
  expect(get).not.toHaveBeenCalled();
});

it("preserves external image URLs on repository routes", async () => {
  renderImage("/repo/space/ship/code/sources/main/README.md", "https://example.com/image.png");
  await expectImage("https://example.com/image.png");
});

it("supports navigating between administration and repository routes", async () => {
  const router = renderImage("/admin/config");
  await expectImage("image.png");
  await act(() => router.navigate("/repo/space/ship/code/sources/main/docs/README.md"));
  await expectImage("/content/main/docs/image.png");
  await act(() => router.navigate("/admin/config"));
  await expectImage("image.png");
});
