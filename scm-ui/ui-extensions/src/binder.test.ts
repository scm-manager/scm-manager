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

import { Binder, ExtensionPointDefinition } from "./binder";

describe("binder tests", () => {
  let binder: Binder;

  beforeEach(() => {
    binder = new Binder("testing");
  });

  it("should return an empty array for non existing extension points", () => {
    const extensions = binder.getExtensions("hitchhiker");
    expect(extensions).toEqual([]);
  });

  it("should return the binded extensions", () => {
    binder.bind("hitchhiker.trillian", "heartOfGold");
    binder.bind("hitchhiker.trillian", "earth");

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["heartOfGold", "earth"]);
  });

  it("should return the first bound extension", () => {
    binder.bind("hitchhiker.trillian", "heartOfGold");
    binder.bind("hitchhiker.trillian", "earth");

    expect(binder.getExtension("hitchhiker.trillian")).toBe("heartOfGold");
  });

  it("should return null if no extension was bound", () => {
    expect(binder.getExtension("hitchhiker.trillian")).toBe(null);
  });

  it("should return true, if an extension is bound", () => {
    binder.bind("hitchhiker.trillian", "heartOfGold");
    expect(binder.hasExtension("hitchhiker.trillian")).toBe(true);
  });

  it("should return false, if no extension is bound", () => {
    expect(binder.hasExtension("hitchhiker.trillian")).toBe(false);
  });

  type Props = {
    category: string;
  };

  it("should return only extensions which predicates matches", () => {
    binder.bind("hitchhiker.trillian", "heartOfGold", (props: Props) => props.category === "a");
    binder.bind("hitchhiker.trillian", "earth", (props: Props) => props.category === "b");
    binder.bind("hitchhiker.trillian", "earth2", (props: Props) => props.category === "a");

    const extensions = binder.getExtensions("hitchhiker.trillian", {
      category: "b"
    });
    expect(extensions).toEqual(["earth"]);
  });

  it("should return extensions in ascending order", () => {
    binder.bind("hitchhiker.trillian", "planetA", () => true, "zeroWaste");
    binder.bind("hitchhiker.trillian", "planetB", () => true, "EPSILON");
    binder.bind("hitchhiker.trillian", "planetC", () => true, "emptyBin");
    binder.bind("hitchhiker.trillian", "planetD", () => true, "absolute");

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["planetD", "planetC", "planetB", "planetA"]);
  });

  it("should return extensions starting with entries with specified extensionName", () => {
    binder.bind("hitchhiker.trillian", "planetA", () => true);
    binder.bind("hitchhiker.trillian", "planetB", () => true, "zeroWaste");
    binder.bind("hitchhiker.trillian", "planetC", () => true);
    binder.bind("hitchhiker.trillian", "planetD", () => true, "emptyBin");

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions[0]).toEqual("planetD");
    expect(extensions[1]).toEqual("planetB");
  });

  it("should allow typings for extension points but still be backwards-compatible", () => {
    type TestExtensionPointA = ExtensionPointDefinition<"test.extension.a", number, undefined>;
    type TestExtensionPointB = ExtensionPointDefinition<"test.extension.b", number, { testProp: boolean[] }>;

    binder.bind<TestExtensionPointA>("test.extension.a", 2, () => false);
    const binderExtensionA = binder.getExtension<TestExtensionPointA>("test.extension.a");
    expect(binderExtensionA).not.toBeNull();
    binder.bind<TestExtensionPointB>("test.extension.b", 2);
    const binderExtensionsB = binder.getExtensions<TestExtensionPointB>("test.extension.b", {
      testProp: [true, false]
    });
    expect(binderExtensionsB).toHaveLength(1);
    binder.bind("test.extension.c", 2, () => false);
    const binderExtensionC = binder.getExtension("test.extension.c");
    expect(binderExtensionC).not.toBeNull();
  });
});
