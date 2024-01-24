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

const HtmlWebpackPlugin = require('html-webpack-plugin');

class RemoveThemesPlugin {
  apply (compiler) {
    compiler.hooks.compilation.tap('RemoveThemesPlugin', (compilation) => {

      HtmlWebpackPlugin.getHooks(compilation).beforeAssetTagGeneration.tapAsync(
        'RemoveThemesPlugin',
        (data, cb) => {

          // remove generated style-loader bundles from the page
          // there should be a better way, which does not generate the bundles at all
          // but for now it works
          if (data.assets.js) {
            data.assets.js = data.assets.js.filter(bundle => !bundle.startsWith("ui-theme-"))
              .filter(bundle => !bundle.startsWith("runtime~ui-theme-"))
          }

          // remove css links to avoid conflicts with the themes
          // so we remove all and add our own via preview-head.html
          if (data.assets.css) {
            data.assets.css = data.assets.css.filter(css => !css.startsWith("ui-theme-"))
          }

          // Tell webpack to move on
          cb(null, data)
        }
      )
    })
  }
}

module.exports = RemoveThemesPlugin
