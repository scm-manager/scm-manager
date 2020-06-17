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

const { ESLint } = require("eslint");
const path = require("path");

const eslint = new ESLint();
const resource = path.join(__dirname, "__resources__");

const lint = async file => {
  const results = await eslint.lintFiles([path.join(resource, file)]);

  const { messages } = results[0];

  const warnings = messages.filter(m => m.severity === 1).map(m => m.ruleId);
  const errors = messages.filter(m => m.severity === 2).map(m => m.ruleId);
  return {
    errors,
    warnings
  };
};

const expectContains = (results, ...ids) => {
  ids.forEach(id => expect(results).toContain(id));
};

describe("should lint different file types", () => {
  it("should lint tsx files", async () => {
    const { errors, warnings } = await lint("TypescriptWithJsx.tsx");
    expectContains(errors, "no-console", "quotes", "semi");
    expectContains(warnings, "prettier/prettier");
  });

  it("should lint js files", async () => {
    const { errors, warnings } = await lint("Node.js");
    expectContains(errors, "no-var", "quotes", "semi");
    expectContains(warnings, "prettier/prettier");
  });

  it("should lint ts files", async () => {
    const { errors, warnings } = await lint("Typescript.ts");
    expectContains(errors, "no-console", "quotes");
    expectContains(warnings, "prettier/prettier");
  });
});

describe("lint @scm-manager imports", () => {
  it("should return an error for source imports", async () => {
    const { errors } = await lint("AvoidSourceImport.tsx");
    expectContains(errors, "no-restricted-imports");
  });

  it("should return no error for package imports", async () => {
    const { errors, warnings } = await lint("AllowRootImport.tsx");
    expect(errors).toEqual([]);
    expect(warnings).toEqual([]);
  });
});
