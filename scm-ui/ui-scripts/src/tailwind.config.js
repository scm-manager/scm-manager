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

const root = path.resolve(process.cwd(), "scm-ui");

const sizes = [0, 1, 2, 3, 4, 5, 6, "auto"];
const helpers = ["m", "p"];
const variants = ["", "x", "y", "t", "r", "l", "b"];
const bulmaHelpers = helpers
  .map((helper) => sizes.map((size) => variants.map((variant) => `${helper}${variant}-${size}`)))
  .flat(3);

module.exports = {
  // eslint-disable-next-line global-require
  presets: [require("@scm-manager/ui-styles/src/tailwind.config.preset")],
  content: [path.join(root, "ui-webapp", "src", "**", "*.tsx")],
  safelist: bulmaHelpers,
};
