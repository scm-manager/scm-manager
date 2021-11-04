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

type Replacement<T> = {
  textToReplace: string;
  replacement: T;
  replaceAll?: boolean;
};

type PartToReplace<T> = {
  start: number;
  length: number;
  replacement: T;
};

function hasConflict<T>(alreadyFoundReplacements: PartToReplace<T>[], newReplacement: PartToReplace<T>) {
  return !!alreadyFoundReplacements.find(
    (existing) =>
      (existing.start <= newReplacement.start && existing.start + existing.length > newReplacement.start) ||
      (newReplacement.start <= existing.start && newReplacement.start + newReplacement.length > existing.start)
  );
}

export default function textSplitAndReplace<T>(
  text: string,
  replacements: Replacement<T>[],
  textWrapper: (s: string) => T
): T[] {
  const partsToReplace: PartToReplace<T>[] = [];

  replacements.forEach((replacement) => {
    let lastIndex = -1;
    do {
      const start = text.indexOf(replacement.textToReplace, lastIndex);
      if (start >= 0) {
        const length = replacement.textToReplace.length;
        const newReplacement = { start, length, replacement: replacement.replacement };
        if (!hasConflict(partsToReplace, newReplacement)) {
          partsToReplace.push(newReplacement);
        }
        lastIndex = start + length;
      } else {
        lastIndex = -1;
      }
    } while (replacement.replaceAll && lastIndex >= 0);
  });

  partsToReplace.sort((a, b) => a.start - b.start);

  const result: T[] = [];

  let lastIndex = 0;
  for (const { start, length, replacement } of partsToReplace) {
    if (start > lastIndex) {
      result.push(textWrapper(text.substr(lastIndex, start - lastIndex)));
    }
    result.push(replacement);
    lastIndex = start + length;
  }
  if (lastIndex < text.length) {
    result.push(textWrapper(text.substr(lastIndex)));
  }

  return result;
}
