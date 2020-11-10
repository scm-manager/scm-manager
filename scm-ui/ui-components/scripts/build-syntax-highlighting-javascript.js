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

// eslint-disable-next-line import/no-extraneous-dependencies
const css = require("css");

// eslint-disable-next-line import/no-extraneous-dependencies
const prettier = require("prettier");

// eslint-disable-next-line import/no-extraneous-dependencies
const camel = require("to-camel-case");

const path = require("path");
const fs = require("fs");

const licenseHeader = `/*
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
 */`;

function createJavascriptStyleSheet(directory, inputFile, outputFile) {
  fs.readFile(path.join(__dirname, `${directory}/${inputFile}`), "utf-8", (err, data) => {
    const javacriptStylesheet = css.parse(data).stylesheet.rules.reduce((sheet, rule) => {
      if (rule.type === "rule") {
        const style = rule.selectors.reduce((selectors, selector) => {
          const selectorObject = rule.declarations.reduce((declarations, declaration) => {
            if (declaration.type === "declaration" && declaration.property) {
              const camelCaseDeclarationProp = camel(declaration.property);
              const key =
                camelCaseDeclarationProp.includes("moz") ||
                camelCaseDeclarationProp.includes("webkit") ||
                (camelCaseDeclarationProp[0] === "o" && !camelCaseDeclarationProp.includes("overflow"))
                  ? `${camelCaseDeclarationProp.substring(0, 1).toUpperCase()}${camelCaseDeclarationProp.substring(1)}`
                  : camelCaseDeclarationProp;
              // eslint-disable-next-line no-param-reassign
              declarations[key] = declaration.value;
            }
            return declarations;
          }, {});

          if (selector.substring(0, 6) === ".token") {
            // eslint-disable-next-line no-param-reassign
            selector = selector.substring(7);

            // Regex to fix Prism theme selectors
            // - Remove further `.token` classes
            // - Remove the space (descendant combinator)
            //   to allow for styling multiple classes
            //   Ref: https://github.com/react-syntax-highlighter/react-syntax-highlighter/pull/305
            // eslint-disable-next-line no-param-reassign
            selector = selector.replace(/(?<=\w) (\.token)?(?=\.)/g, "");
          }
          // eslint-disable-next-line no-param-reassign
          selectors[selector] = selectorObject;
          return selectors;
        }, {});
        // eslint-disable-next-line no-param-reassign
        sheet = Object.keys(style).reduce((stylesheet, selector) => {
          if (stylesheet[selector]) {
            // eslint-disable-next-line
            stylesheet[selector] = { ...stylesheet[selector], ...style[selector] };
          } else {
            // eslint-disable-next-line no-param-reassign
            stylesheet[selector] = style[selector];
          }
          return stylesheet;
        }, sheet);
      }
      return sheet;
    }, {});
    fs.writeFile(
      path.join(__dirname, directory, outputFile),
      prettier.format(`${licenseHeader}
      
/* --- DO NOT EDIT --- */
/* Auto-generated from ${inputFile} */
      
      export default ${JSON.stringify(javacriptStylesheet, null, 2)}`),
      () => {}
    );
  });
}

createJavascriptStyleSheet(path.join("..", "src"), "syntax-highlighting.module.css", "syntax-highlighting.ts");
