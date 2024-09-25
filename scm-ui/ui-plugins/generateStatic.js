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

import fs from "node:fs";

const packageJson = JSON.parse(fs.readFileSync("./package.json", "utf8"));

const { lazyDependencies } = packageJson;

let importStatements = "";
let calls = "";

Object.keys(packageJson.dependencies)
  .filter((dependency) => !lazyDependencies.includes(dependency))
  .forEach((dependency, index) => {
    importStatements += `import * as DEP${index} from '${dependency}';\n`;
    calls +=  `  defineStatic('${dependency}', DEP${index});\n`;
  });

fs.mkdirSync("./build", { recursive: true });
fs.writeFileSync(
  "./build/provided-modules.d.ts",
  `
type DefineStatic = (name: string, dependency: unknown) => void;

declare function definePluginDependencies(defineStatic: DefineStatic): void;
export {definePluginDependencies};
`
);
fs.writeFileSync(
  "./build/provided-modules.js",
  `
${importStatements}

export function definePluginDependencies(defineStatic) {
${calls}
}`
);
