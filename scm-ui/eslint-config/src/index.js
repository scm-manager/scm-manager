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

const rules = {
  "prettier/prettier": "warn",
  semi: ["error", "always"],
  quotes: ["error", "double", "avoid-escape"],
  "no-var": "error"
};

const nodeConfiguration = {
  extends: ["airbnb-base", "plugin:prettier/recommended"],
  rules: {
    "no-console": "off",
    ...rules
  }
};

const restrictImportConfig = {
  patterns: ["@scm-manager/*/*"]
};

const typescriptConfiguration = {
  parser: "@typescript-eslint/parser",
  extends: ["react-app", "plugin:@typescript-eslint/recommended"],
  rules: {
    "@typescript-eslint/explicit-function-return-type": "off",
    "@typescript-eslint/ban-ts-ignore": "warn",
    "no-console": "error",
    "jsx-a11y/href-no-hash": "off",
    "no-restricted-imports": ["error", restrictImportConfig],
    ...rules
  }
};

module.exports = {
  overrides: [
    {
      files: ["*.test.js"],
      env: {
        node: true,
        jest: true,
        browser: false
      },
      ...nodeConfiguration
    },
    {
      files: ["*.js"],
      env: {
        node: true,
        browser: false
      },
      ...nodeConfiguration
    },
    {
      files: ["*.test.ts", "*.test.tsx"],
      env: {
        node: true,
        jest: true,
        browser: false
      },
      ...typescriptConfiguration
    },
    {
      files: ["*.ts", "*.tsx"],
      env: {
        node: false,
        browser: true
      },
      ...typescriptConfiguration
    }
  ]
};
