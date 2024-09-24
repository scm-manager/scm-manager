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
import { Binder, ExtensionPointDefinition, SimpleDynamicExtensionPointDefinition } from "./binder";
import ExtensionPoint, { RenderableExtensionPointDefinition } from "./ExtensionPoint";

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
      category: "b",
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

    binder.bind<TestExtensionPointA>("test.extension.a", 2, () => true);
    const binderExtensionA = binder.getExtension<TestExtensionPointA>("test.extension.a");
    expect(binderExtensionA).not.toBeNull();
    binder.bind<TestExtensionPointB>("test.extension.b", 2);
    const binderExtensionsB = binder.getExtensions<TestExtensionPointB>("test.extension.b", {
      testProp: [true, false],
    });
    expect(binderExtensionsB).toHaveLength(1);
    binder.bind("test.extension.c", 2, () => true);
    const binderExtensionC = binder.getExtension("test.extension.c");
    expect(binderExtensionC).not.toBeNull();
  });

  it("should allow typings for renderable extension points", () => {
    type TestExtensionPointA = RenderableExtensionPointDefinition<"test.extension.a">;
    type TestExtensionPointB = RenderableExtensionPointDefinition<"test.extension.b", { testProp: boolean[] }>;

    binder.bind<TestExtensionPointA>(
      "test.extension.a",
      () => <h1>Hello world</h1>,
      () => true
    );
    const binderExtensionA = binder.getExtension<TestExtensionPointA>("test.extension.a");
    expect(binderExtensionA).not.toBeNull();
    binder.bind<TestExtensionPointB>("test.extension.b", ({ testProp }) => (
      <h1>
        {testProp.map((b) => (
          <span>{b}</span>
        ))}
      </h1>
    ));
    const binderExtensionsB = binder.getExtensions<TestExtensionPointB>("test.extension.b", {
      testProp: [true, false],
    });
    expect(binderExtensionsB).toHaveLength(1);
  });

  it("should render typed extension point", () => {
    type TestExtensionPointA = RenderableExtensionPointDefinition<"test.extension.a">;
    type TestExtensionPointB = RenderableExtensionPointDefinition<"test.extension.b", { testProp: boolean[] }>;

    binder.bind<TestExtensionPointA>(
      "test.extension.a",
      () => <h1>Hello world</h1>,
      () => false
    );
    const binderExtensionA = <ExtensionPoint<TestExtensionPointA> name="test.extension.a" />;
    expect(binderExtensionA).not.toBeNull();
    binder.bind<TestExtensionPointB>("test.extension.b", ({ testProp }) => (
      <h1>
        {testProp.map((b) => (
          <span>{b}</span>
        ))}
      </h1>
    ));
    const binderExtensionsB = (
      <ExtensionPoint<TestExtensionPointB>
        name="test.extension.b"
        props={{
          testProp: [true, false],
        }}
      />
    );
    expect(binderExtensionsB).not.toBeNull();
  });

  it("should allow typings for dynamic extension points", () => {
    type MarkdownCodeLanguageRendererProps = {
      language?: string;
      value: string;
    };

    type MarkdownCodeLanguageRendererExtensionPoint<S extends string | undefined = undefined> =
      SimpleDynamicExtensionPointDefinition<
        "markdown-renderer.code.",
        (props: any) => any,
        MarkdownCodeLanguageRendererProps,
        S
      >;
    type UmlExtensionPoint = MarkdownCodeLanguageRendererExtensionPoint<"uml">;

    binder.bind<UmlExtensionPoint>("markdown-renderer.code.uml", (props) => props.value);

    const language = "uml";
    const extensionPointName = `markdown-renderer.code.${language}` as const;
    const dynamicExtension = binder.getExtension<MarkdownCodeLanguageRendererExtensionPoint>(extensionPointName, {
      language: "uml",
      value: "const a = 2;",
    });
    expect(dynamicExtension).not.toBeNull();
  });

  it("should allow options parameter", () => {
    binder.bind("hitchhiker.trillian", "planetA", {
      predicate: () => true,
      extensionName: "zeroWaste",
    });

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["planetA"]);
  });

  it("should allow empty options parameter", () => {
    binder.bind("hitchhiker.trillian", "planetA", {});

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["planetA"]);
  });

  it("should allow options parameter with only predicate", () => {
    binder.bind("hitchhiker.trillian", "planetA", {
      predicate: () => true,
    });

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["planetA"]);
  });

  it("should allow options parameter with only extensionName", () => {
    binder.bind("hitchhiker.trillian", "planetA", {
      extensionName: "zeroWaste",
    });

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["planetA"]);
  });

  it("should order by priority in descending order", () => {
    binder.bind("hitchhiker.trillian", "planetA", { priority: 10 });
    binder.bind("hitchhiker.trillian", "planetB", { priority: 50 });
    binder.bind("hitchhiker.trillian", "planetC", { priority: 100 });
    binder.bind("hitchhiker.trillian", "planetD", { priority: 75 });

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["planetC", "planetD", "planetB", "planetA"]);
  });

  it("should order by priority over ordering by name", () => {
    binder.bind("hitchhiker.trillian", "planetA", { priority: 10, extensionName: "ignore" });
    binder.bind("hitchhiker.trillian", "planetB", { priority: 50 });
    binder.bind("hitchhiker.trillian", "planetC", { priority: 100, extensionName: "me" });
    binder.bind("hitchhiker.trillian", "planetD", { priority: 75 });

    const extensions = binder.getExtensions("hitchhiker.trillian");
    expect(extensions).toEqual(["planetC", "planetD", "planetB", "planetA"]);
  });
});
