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
import "string_score";

// typing for string_score
declare global {
  interface String {
    score(query: string): number;
  }
}

export const filepathSearch = (paths: string[], query: string): string[] => {
  return paths
    .map(createMatcher(query))
    .filter((m) => m.matches)
    .sort((a, b) => b.score - a.score)
    .slice(0, 50)
    .map((m) => m.path);
};

const includes = (value: string, query: string) => {
  return value.toLocaleLowerCase("en").includes(query.toLocaleLowerCase("en"));
};

export const createMatcher = (query: string) => {
  return (path: string) => {
    const parts = path.split("/");
    const filename = parts[parts.length - 1];

    let score = filename.score(query);
    if (score > 0 && includes(filename, query)) {
      score += 0.5;
    } else if (score <= 0) {
      score = path.score(query) * 0.25;
    }

    return {
      matches: score > 0,
      score,
      path,
    };
  };
};
