import { chooseLocale, supportedLocales } from "./DateFromNow";

describe("test choose locale", () => {

  it("should choose de", () => {
    const locale = chooseLocale("de_DE", ["de", "en"]);
    expect(locale).toBe(supportedLocales.de);
  });

  it("should choose de, even without language array", () => {
    const locale = chooseLocale("de", []);
    expect(locale).toBe(supportedLocales.de);
  });

  it("should choose es", () => {
    const locale = chooseLocale("de", ["af", "be", "es"]);
    expect(locale).toBe(supportedLocales.es);
  });

  it("should fallback en", () => {
    const locale = chooseLocale("af", ["af", "be"]);
    expect(locale).toBe(supportedLocales.en);
  });

});
