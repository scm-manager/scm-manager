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
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import { useUpdateInfo } from "./admin";
import { UpdateInfo } from "@scm-manager/ui-types";
import { setIndexLink } from "./tests/indexLinks";

describe("Test admin hooks", () => {
  describe("useUpdateInfo tests", () => {
    it("should get update info", async () => {
      const updateInfo: UpdateInfo = {
        latestVersion: "x.y.z",
        link: "http://heartofgold@hitchhiker.com/x.y.z",
      };
      fetchMock.getOnce("/api/v2/updateInfo", updateInfo);

      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "updateInfo", "/updateInfo");

      const { result, waitFor } = renderHook(() => useUpdateInfo(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      expect(result.current.data).toEqual(updateInfo);
    });
  });
});
