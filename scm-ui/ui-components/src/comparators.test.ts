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

import { byKey, byValueLength, byNestedKeys } from "./comparators";

const createObject = (key: string, value?: string) => {
  return {
    [key]: value,
  };
};

const createObjects = (key: string, values: Array<string | undefined>) => {
  return values.map((v) => createObject(key, v));
};

describe("key comparator tests", () => {
  it("should sort array", () => {
    const array = createObjects("key", ["z", "a", "y", "b"]);
    array.sort(byKey("key"));
    expect(array).toEqual(createObjects("key", ["a", "b", "y", "z"]));
  });

  it("should not fail if value is undefined", () => {
    const array = createObjects("key", ["z", undefined, "a"]);
    array.sort(byKey("key"));
    expect(array).toEqual(createObjects("key", ["a", "z", undefined]));
  });

  it("should not fail if key is undefined", () => {
    const array = createObjects("key", ["a"]);
    array.push({});
    array.push(createObject("key", "z"));
    array.sort(byKey("key"));
    expect(array).toEqual([createObject("key", "a"), createObject("key", "z"), {}]);
  });

  it("should not fail if item is undefined", () => {
    const array: unknown[] = createObjects("key", ["a"]);
    array.push(undefined);
    array.push(createObject("key", "z"));
    array.sort(byKey("key"));
    expect(array).toEqual([createObject("key", "a"), createObject("key", "z"), undefined]);
  });
});

describe("length comparator tests", () => {
  it("should sort array", () => {
    const array = createObjects("key", ["....", ".", "...", ".."]);
    array.sort(byValueLength("key"));
    expect(array).toEqual(createObjects("key", [".", "..", "...", "...."]));
  });

  it("should not fail if value is undefined", () => {
    const array = createObjects("key", ["..", undefined, "."]);
    array.sort(byValueLength("key"));
    expect(array).toEqual(createObjects("key", [".", "..", undefined]));
  });

  it("should not fail if key is undefined", () => {
    const array = createObjects("key", ["."]);
    array.push({});
    array.push(createObject("key", ".."));
    array.sort(byValueLength("key"));
    expect(array).toEqual([createObject("key", "."), createObject("key", ".."), {}]);
  });

  it("should not fail if item is undefined", () => {
    const array: unknown[] = createObjects("key", ["."]);
    array.push(undefined);
    array.push(createObject("key", ".."));
    array.sort(byValueLength("key"));
    expect(array).toEqual([createObject("key", "."), createObject("key", ".."), undefined]);
  });
});

describe("nested key comparator tests", () => {
  const createObject = (key: string, nested?: string, value?: string) => {
    if (!nested) {
      return {
        [key]: undefined,
      };
    }
    return {
      [key]: {
        [nested]: value,
      },
    };
  };

  const createObjects = (key: string, nested: string, values: Array<string | undefined>) => {
    return values.map((v) => createObject(key, nested, v));
  };

  it("should sort array", () => {
    const array = createObjects("key", "nested", ["z", "a", "y", "b"]);
    array.sort(byNestedKeys("key", "nested"));
    expect(array).toEqual(createObjects("key", "nested", ["a", "b", "y", "z"]));
  });

  it("should not fail if value is undefined", () => {
    const array = createObjects("key", "nested", ["z", undefined, "a"]);
    array.sort(byNestedKeys("key", "nested"));
    expect(array).toEqual(createObjects("key", "nested", ["a", "z", undefined]));
  });

  it("should not fail if key is undefined", () => {
    const array = createObjects("key", "nested", ["a"]);
    array.push({});
    array.push(createObject("key", "nested", "z"));
    array.sort(byNestedKeys("key", "nested"));
    expect(array).toEqual([createObject("key", "nested", "a"), createObject("key", "nested", "z"), {}]);
  });

  it("should not fail if nested key is undefined", () => {
    const array = createObjects("key", "nested", ["a"]);
    array.push(createObject("key", undefined, "y"));
    array.push(createObject("key", "nested", "z"));
    array.sort(byNestedKeys("key", "nested"));
    expect(array).toEqual([createObject("key", "nested", "a"), createObject("key", "nested", "z"), { key: undefined }]);
  });

  it("should not fail if item is undefined", () => {
    const array: unknown[] = createObjects("key", "nested", ["a"]);
    array.push(undefined);
    array.push(createObject("key", "nested", "z"));
    array.sort(byNestedKeys("key", "nested"));
    expect(array).toEqual([createObject("key", "nested", "a"), createObject("key", "nested", "z"), undefined]);
  });
});
