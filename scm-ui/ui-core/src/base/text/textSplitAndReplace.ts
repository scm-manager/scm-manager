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
