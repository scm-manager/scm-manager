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
