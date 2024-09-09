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
