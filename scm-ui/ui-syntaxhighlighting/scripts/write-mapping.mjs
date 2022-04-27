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

import fs from "fs/promises";
import path from "path";
import { refractor } from "refractor/lib/all.js";

const findAliases = async (lang) => {
  const mod = await import(`refractor/lang/${lang}.js`);
  return mod.default.aliases;
};

const createMapping = async () => {
  const langs = refractor.listLanguages();
  const mapping = {};
  for (let lang of langs) {
    try {
      const aliases = await findAliases(lang);
      for (let alias of aliases) {
        mapping[alias] = lang;
      }
    } catch (e) {
      console.log(`failed to find aliases for ${lang}`);
    }
  }

  return mapping;
};

const writeMapping = async () => {
  const mapping = await createMapping();
  const filepath = path.join(process.cwd(), "src", "worker", "mapping.json");
  await fs.writeFile(filepath, JSON.stringify(mapping, null, 2), { encoding: "utf-8" });
};

writeMapping().then(() => console.log("successfully written mapping"));
