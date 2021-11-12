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
