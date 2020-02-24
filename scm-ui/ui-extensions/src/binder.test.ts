import { Binder } from "./binder";

describe("binder tests", () => {
  let binder: Binder;

  beforeEach(() => {
    binder = new Binder();
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
});
