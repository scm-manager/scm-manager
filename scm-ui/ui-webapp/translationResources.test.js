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

const fs = require("fs");

const dirs = fs.readdirSync("public/locales");

describe("locales folder", () => {
  it("should contain folder en", () => {
    expect(dirs).toContain("en");
  });
});

dirs.forEach(languageDirName => {
  const languageDir = `public/locales/${languageDirName}`;
  const languageDirFiles = fs.readdirSync(languageDir);
  languageDirFiles
    .filter(fileName => fileName.endsWith(".json"))
    .forEach(translationFileName => {
      const translationFile = `${languageDir}/${translationFileName}`;
      describe(`translation file ${translationFile}`, () => {
        it("should contain only valid json", () => {
          const jsonContent = fs.readFileSync(translationFile);
          let json;
          try {
            json = JSON.parse(jsonContent);
          } catch (ex) {
            // eslint-disable-next-line no-undef
            fail(new Error(`invalid json: ${ex}`));
          }
          expect(json).not.toBe({});
        });
      });
    });
});
