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
import { File, Repository } from "@scm-manager/ui-types";
import { useSources } from "./sources";
import fetchMock from "fetch-mock";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { act, renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";

describe("Test sources hooks", () => {
  const puzzle42: Repository = {
    namespace: "puzzles",
    name: "42",
    type: "git",
    _links: {
      sources: {
        href: "/src",
      },
    },
  };

  const readmeMd: File = {
    name: "README.md",
    path: "README.md",
    directory: false,
    revision: "abc",
    length: 21,
    description: "Awesome readme",
    _links: {},
    _embedded: {
      children: [],
    },
  };

  const rootDirectory: File = {
    name: "",
    path: "",
    directory: true,
    revision: "abc",
    _links: {},
    _embedded: {
      children: [readmeMd],
    },
  };

  const sepecialMd: File = {
    name: "special.md",
    path: "main/special.md",
    directory: false,
    revision: "abc",
    length: 42,
    description: "Awesome special file",
    _links: {},
    _embedded: {
      children: [],
    },
  };

  const sepecialMdPartial: File = {
    ...sepecialMd,
    partialResult: true,
    computationAborted: false,
  };

  const sepecialMdComputationAborted: File = {
    ...sepecialMd,
    partialResult: true,
    computationAborted: true,
  };

  const mainDirectoryTruncated: File = {
    name: "main",
    path: "main",
    directory: true,
    revision: "abc",
    truncated: true,
    _links: {
      proceed: {
        href: "src/2",
      },
    },
    _embedded: {
      children: [],
    },
  };

  const mainDirectory: File = {
    ...mainDirectoryTruncated,
    truncated: false,
    _embedded: {
      children: [sepecialMd],
    },
  };

  beforeEach(() => {
    fetchMock.reset();
  });

  const firstChild = (directory?: File) => {
    if (directory?._embedded?.children && directory._embedded.children.length > 0) {
      return directory._embedded.children[0];
    }
  };

  describe("useSources tests", () => {
    const testPath = async (path: string, expectedPath: string) => {
      const queryClient = createInfiniteCachingClient();
      fetchMock.getOnce(`/api/v2/src/abc/${expectedPath}?collapse=true`, readmeMd);
      const { result, waitFor } = renderHook(() => useSources(puzzle42, { revision: "abc", path }), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(readmeMd);
    };

    it("should return file from url with revision and path", () => testPath("README.md", "README.md"));
    it("should encode square brackets in path", () => testPath("[...nextauth].ts", "%5B...nextauth%5D.ts"));
    it("should not double-encode path", () => testPath("%7BDatei%7D#42.txt", "%7BDatei%7D#42.txt"));

    it("should return root directory", async () => {
      const queryClient = createInfiniteCachingClient();
      fetchMock.getOnce("/api/v2/src?collapse=true", rootDirectory);
      const { result, waitFor } = renderHook(() => useSources(puzzle42), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(rootDirectory);
    });

    it("should fetch next page", async () => {
      const queryClient = createInfiniteCachingClient();
      fetchMock.getOnce("/api/v2/src?collapse=true", mainDirectoryTruncated);
      fetchMock.getOnce("/api/v2/src/2", mainDirectory);
      const { result, waitFor, waitForNextUpdate } = renderHook(() => useSources(puzzle42), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);

      expect(result.current.data).toEqual(mainDirectoryTruncated);

      await act(() => {
        const { fetchNextPage } = result.current;
        fetchNextPage();
        return waitForNextUpdate();
      });
      await waitFor(() => !result.current.isFetchingNextPage);

      expect(result.current.data).toEqual(mainDirectory);
    });

    it("should refetch if partial files exists", async () => {
      const queryClient = createInfiniteCachingClient();
      fetchMock.get(
        "/api/v2/src?collapse=true",
        {
          ...mainDirectory,
          _embedded: {
            children: [sepecialMdPartial],
          },
        },
        {
          repeat: 1,
        }
      );
      fetchMock.get(
        "/api/v2/src?collapse=true",
        {
          ...mainDirectory,
          _embedded: {
            children: [sepecialMd],
          },
        },
        {
          repeat: 1,
          overwriteRoutes: false,
        }
      );

      const { result, waitFor } = renderHook(() => useSources(puzzle42, { refetchPartialInterval: 100 }), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await waitFor(() => !!firstChild(result.current.data));
      expect(firstChild(result.current.data)?.partialResult).toBe(true);

      await waitFor(() => !firstChild(result.current.data)?.partialResult);
      expect(firstChild(result.current.data)?.partialResult).toBeFalsy();
    });

    it("should not refetch if computation is aborted", async () => {
      const queryClient = createInfiniteCachingClient();
      fetchMock.getOnce("/api/v2/src/abc/main/special.md?collapse=true", sepecialMdComputationAborted, { repeat: 1 });
      // should never be called
      fetchMock.getOnce("/api/v2/src/abc/main/special.md?collapse=true", sepecialMd, {
        repeat: 1,
        overwriteRoutes: false,
      });
      const { result, waitFor } = renderHook(
        () =>
          useSources(puzzle42, {
            revision: "abc",
            path: "main/special.md",
            refetchPartialInterval: 100,
          }),
        {
          wrapper: createWrapper(undefined, queryClient),
        }
      );
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(sepecialMdComputationAborted);

      await new Promise((r) => setTimeout(r, 200));
      expect(result.current.data).toEqual(sepecialMdComputationAborted);
    });
  });
});
