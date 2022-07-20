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

const path = require("path");

const sizes = [1, 2, 3, 4, 5, 6];
const helpers = ["m", "p"];
const variants = ["", "x", "y", "t", "r", "l", "b"];
const bulmaHelpers = helpers
  .map((helper) => sizes.map((size) => variants.map((variant) => `${helper}${variant}-${size}`)))
  .flat(3);

module.exports = {
  content: [path.join(__dirname, "src/**/*.tsx")],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "var(--scm-primary-color)",
          hover: "var(--scm-primary-hover-color)",
          active: "var(--scm-primary-active-color)",
          contrast: "var(--scm-primary-contrast-color)",
          "hover-contrast": "var(--scm-primary-hover-contrast-color)",
          "active-contrast": "var(--scm-primary-active-contrast-color)",
        },
        signal: {
          DEFAULT: "var(--scm-warning-color)",
          hover: "var(--scm-warning-hover-color)",
          active: "var(--scm-warning-active-color)",
          contrast: "var(--scm-warning-contrast-color)",
        },
      },
    },
  },
  safelist: bulmaHelpers,
  plugins: [],
  important: true,
};
