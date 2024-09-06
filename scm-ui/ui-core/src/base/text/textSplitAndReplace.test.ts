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

import textSplitAndReplace from "./textSplitAndReplace";

type Wrapped = {
  text: string;
};

const testWrapper = (s: string) => {
  return { text: s };
};

describe("text split and replace", () => {
  it("should wrap text if nothing should be replaced", () => {
    const result = textSplitAndReplace<Wrapped>("Don't Panic.", [], testWrapper);
    expect(result).toHaveLength(1);
    expect(result[0]).toStrictEqual({ text: "Don't Panic." });
  });

  it("should replace single string", () => {
    const result = textSplitAndReplace<Wrapped>(
      "Don't Panic.",
      [{ textToReplace: "'", replacement: { text: "`" } }],
      testWrapper
    );
    expect(result).toHaveLength(3);
    expect(result[0]).toStrictEqual({ text: "Don" });
    expect(result[1]).toStrictEqual({ text: "`" });
    expect(result[2]).toStrictEqual({ text: "t Panic." });
  });

  it("should replace strings only once if replace all is not set", () => {
    const result = textSplitAndReplace<Wrapped>(
      "'So this is it,' said Arthur, 'We are going to die.'",
      [{ textToReplace: "'", replacement: { text: "“" } }],
      testWrapper
    );
    expect(result).toHaveLength(2);
    expect(result[0]).toStrictEqual({ text: "“" });
    expect(result[1]).toStrictEqual({ text: "So this is it,' said Arthur, 'We are going to die.'" });
  });

  it("should replace all strings if replace all is set to true", () => {
    const result = textSplitAndReplace<Wrapped>(
      "'So this is it,' said Arthur, 'We are going to die.'",
      [{ textToReplace: "'", replacement: { text: "“" }, replaceAll: true }],
      testWrapper
    );
    expect(result).toHaveLength(7);
    expect(result[0]).toStrictEqual({ text: "“" });
    expect(result[1]).toStrictEqual({ text: "So this is it," });
    expect(result[2]).toStrictEqual({ text: "“" });
    expect(result[3]).toStrictEqual({ text: " said Arthur, " });
    expect(result[4]).toStrictEqual({ text: "“" });
    expect(result[5]).toStrictEqual({ text: "We are going to die." });
    expect(result[6]).toStrictEqual({ text: "“" });
  });

  it("should replace strings with multiple replacements", () => {
    const result = textSplitAndReplace<Wrapped>(
      "'So this is it,' said Arthur, 'We are going to die.'",
      [
        { textToReplace: "'", replacement: { text: "“" }, replaceAll: true },
        { textToReplace: "Arthur", replacement: { text: "Dent" }, replaceAll: true },
      ],
      testWrapper
    );
    expect(result).toHaveLength(9);
    expect(result[0]).toStrictEqual({ text: "“" });
    expect(result[1]).toStrictEqual({ text: "So this is it," });
    expect(result[2]).toStrictEqual({ text: "“" });
    expect(result[3]).toStrictEqual({ text: " said " });
    expect(result[4]).toStrictEqual({ text: "Dent" });
    expect(result[5]).toStrictEqual({ text: ", " });
    expect(result[6]).toStrictEqual({ text: "“" });
    expect(result[7]).toStrictEqual({ text: "We are going to die." });
    expect(result[8]).toStrictEqual({ text: "“" });
  });

  it("should ignore conflicting replacements", () => {
    const result = textSplitAndReplace<Wrapped>(
      "'So this is it,' said Arthur, 'We are going to die.'",
      [
        { textToReplace: "said Arthur", replacement: { text: "to be replaced" } },
        { textToReplace: " said", replacement: { text: "to be ignored 1" }, replaceAll: true },
        { textToReplace: "d A", replacement: { text: "to be ignored 2" }, replaceAll: true },
        { textToReplace: "Arthur,", replacement: { text: "to be ignored 3" }, replaceAll: true },
      ],
      testWrapper
    );
    expect(result).toHaveLength(3);
    expect(result[0]).toStrictEqual({ text: "'So this is it,' " });
    expect(result[1]).toStrictEqual({ text: "to be replaced" });
    expect(result[2]).toStrictEqual({ text: ", 'We are going to die.'" });
  });

  it("should replace adjacent texts", () => {
    const result = textSplitAndReplace<Wrapped>(
      "'So this is it,' said Arthur, 'We are going to die.'",
      [
        { textToReplace: "'So this is it,'", replacement: { text: "one" } },
        { textToReplace: " said Arthur, ", replacement: { text: "two" } },
        { textToReplace: "'We are going to die.'", replacement: { text: "three" } },
      ],
      testWrapper
    );
    expect(result).toHaveLength(3);
    expect(result[0]).toStrictEqual({ text: "one" });
    expect(result[1]).toStrictEqual({ text: "two" });
    expect(result[2]).toStrictEqual({ text: "three" });
  });
});
