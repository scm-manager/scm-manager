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

import fetchMock from "fetch-mock-jest";
import { Changeset } from "@scm-manager/ui-types";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import { RevertRequest, RevertResponse, useRevert } from "./revert";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";

const queryClient = createInfiniteCachingClient();

const mockChangeset: Changeset = {
  id: "0f3cdd",
  description: "Awesome change",
  date: new Date(),
  author: {
    name: "Arthur Dent",
  },
  _embedded: {},
  _links: {
    revert: {
      href: "/hitchhiker/heart-of-gold/changesets/0f3cdd/revert",
    },
  },
};

const expectedRevision: RevertResponse = {
  revision: "a3ffde",
};

const revertRequest: RevertRequest = {
  branch: "captain/kirk",
  message: "Hello World!",
};

beforeEach(() => queryClient.clear());

afterEach(() => {
  fetchMock.reset();
});

describe("useRevert tests", () => {
  const fetchRevert = async (changeset: Changeset, request: RevertRequest) => {
    fetchMock.postOnce("api/v2/hitchhiker/heart-of-gold/changesets/0f3cdd/revert", expectedRevision);
    const { result: useRevertResult, waitFor } = renderHook(() => useRevert(changeset), {
      wrapper: createWrapper(undefined, queryClient),
    });

    await waitFor(() => {
      return !!useRevertResult.current.revert || !!useRevertResult.current.error;
    });

    const { waitFor: waitForRevert } = renderHook(() => useRevertResult.current.revert(request), {
      wrapper: createWrapper(undefined, queryClient),
    });

    await waitForRevert(() => {
      return !!useRevertResult.current.revision || !!useRevertResult.current.error;
    });

    return useRevertResult.current;
  };

  it("should return revision from revert", async () => {
    const { revision, error } = await fetchRevert(mockChangeset, revertRequest);
    expect(revision).toEqual(expectedRevision);
    expect(error).toBeNull();
  });
});
