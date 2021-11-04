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

/* --- DO NOT EDIT --- */
/* Auto-generated from syntax-highlighting.module.css */

export default {
  'pre[class*="language-"]': {
    color: "var(--sh-base-color)",
    fontSize: "1rem",
    textShadow: "none",
    fontFamily: "var(--sh-font-family)",
    direction: "ltr",
    textAlign: "left",
    whiteSpace: "pre",
    wordSpacing: "normal",
    wordBreak: "normal",
    lineHeight: "1.5",
    MozTabSize: "4",
    OTabSize: "4",
    tabSize: "4",
    WebkitHyphens: "none",
    MozHyphens: "none",
    msHyphens: "none",
    hyphens: "none",
    padding: "1em",
    margin: ".5em 0",
    overflow: "auto",
    background: "var(--sh-block-background)",
  },
  'code[class*="language-"]': {
    color: "var(--sh-base-color)",
    fontSize: "1rem",
    textShadow: "none",
    fontFamily: "var(--sh-font-family)",
    direction: "ltr",
    textAlign: "left",
    whiteSpace: "pre",
    wordSpacing: "normal",
    wordBreak: "normal",
    lineHeight: "1.5",
    MozTabSize: "4",
    OTabSize: "4",
    tabSize: "4",
    WebkitHyphens: "none",
    MozHyphens: "none",
    msHyphens: "none",
    hyphens: "none",
  },
  'pre[class*="language-"]::selection': {
    textShadow: "none",
    background: "var(--sh-selected-color)",
  },
  'code[class*="language-"]::selection': {
    textShadow: "none",
    background: "var(--sh-selected-color)",
  },
  'pre[class*="language-"]::-moz-selection': {
    textShadow: "none",
    background: "var(--sh-selected-color)",
  },
  'code[class*="language-"]::-moz-selection': {
    textShadow: "none",
    background: "var(--sh-selected-color)",
  },
  ':not(pre) > code[class*="language-"]': {
    padding: ".1em .3em",
    borderRadius: ".3em",
    color: "var(--sh-inline-code-color)",
    background: "var(--sh-inline-code-background)",
  },
  ".namespace": {
    Opacity: ".7",
  },
  comment: {
    color: "var(--sh-comment-color)",
  },
  prolog: {
    color: "var(--sh-comment-color)",
  },
  doctype: {
    color: "var(--sh-comment-color)",
  },
  cdata: {
    color: "var(--sh-comment-color)",
  },
  punctuation: {
    color: "var(--sh-punctuation-color)",
  },
  property: {
    color: "var(--sh-property-color)",
  },
  tag: {
    color: "var(--sh-property-color)",
  },
  boolean: {
    color: "var(--sh-property-color)",
  },
  number: {
    color: "var(--sh-property-color)",
  },
  constant: {
    color: "var(--sh-property-color)",
  },
  symbol: {
    color: "var(--sh-property-color)",
  },
  deleted: {
    color: "var(--sh-property-color)",
  },
  selector: {
    color: "var(--sh-selector-color)",
  },
  "attr-name": {
    color: "var(--sh-selector-color)",
  },
  string: {
    color: "var(--sh-selector-color)",
  },
  char: {
    color: "var(--sh-selector-color)",
  },
  builtin: {
    color: "var(--sh-selector-color)",
  },
  inserted: {
    color: "var(--sh-selector-color)",
  },
  operator: {
    color: "var(--sh-operator-color)",
    background: "var(--sh-operator-bg)",
  },
  entity: {
    color: "var(--sh-operator-color)",
    background: "var(--sh-operator-bg)",
    cursor: "help",
  },
  url: {
    color: "var(--sh-operator-color)",
    background: "var(--sh-operator-bg)",
  },
  ".language-css .token.string": {
    color: "var(--sh-operator-color)",
    background: "var(--sh-operator-bg)",
  },
  ".style .token.string": {
    color: "var(--sh-operator-color)",
    background: "var(--sh-operator-bg)",
  },
  atrule: {
    color: "var(--sh-keyword-color)",
  },
  "attr-value": {
    color: "var(--sh-keyword-color)",
  },
  keyword: {
    color: "var(--sh-keyword-color)",
  },
  function: {
    color: "var(--sh-function-color)",
  },
  regex: {
    color: "var(--sh-variable-color)",
  },
  important: {
    color: "var(--sh-variable-color)",
    fontWeight: "bold",
  },
  variable: {
    color: "var(--sh-variable-color)",
  },
  bold: {
    fontWeight: "bold",
  },
  title: {
    fontWeight: "bold",
  },
  italic: {
    fontStyle: "italic",
  },
  "pre[data-line]": {
    position: "relative",
  },
  'pre[class*="language-"] > code[class*="language-"]': {
    position: "relative",
    zIndex: "1",
  },
  ".line-highlight": {
    position: "absolute",
    left: "0",
    right: "0",
    padding: "inherit 0",
    marginTop: "1em",
    background: "var(--sh-highlight-background)",
    boxShadow: "inset 5px 0 0 var(--sh-highlight-accent)",
    zIndex: "0",
    pointerEvents: "none",
    lineHeight: "inherit",
    whiteSpace: "pre",
  },
};
