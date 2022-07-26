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
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "var(--scm-primary-color)",
          contrast: "var(--scm-primary-contrast-color)",
          hover: "var(--scm-primary-hover-color)",
          "hover-contrast": "var(--scm-primary-hover-contrast-color)",
          active: "var(--scm-primary-active-color)",
          "active-contrast": "var(--scm-primary-active-contrast-color)",
          disabled: "var(--scm-primary-disabled-color)",
          "disabled-contrast": "var(--scm-primary-disabled-contrast-color)",
        },
        signal: {
          DEFAULT: "var(--scm-warning-color)",
          contrast: "var(--scm-warning-contrast-color)",
          hover: "var(--scm-warning-hover-color)",
          "hover-contrast": "var(--scm-warning-hover-contrast-color)",
          active: "var(--scm-warning-active-color)",
          "active-contrast": "var(--scm-warning-active-contrast-color)",
          disabled: "var(--scm-warning-disabled-color)",
          "disabled-contrast": "var(--scm-warning-disabled-contrast-color)",
        },
      },
    },
  },
  important: true,
};
