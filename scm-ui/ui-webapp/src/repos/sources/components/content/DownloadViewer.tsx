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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { DownloadButton } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";

type Props = WithTranslation & {
  repository: Repository;
  file: File;
};

class DownloadViewer extends React.Component<Props> {
  render() {
    const { t, repository, file } = this.props;

    return (
      <div className="has-text-centered">
        <ExtensionPoint name="repos.sources.content.downloadButton" props={{ repository, file }}>
          <DownloadButton url={(file._links.self as Link).href} displayName={t("sources.content.downloadButton")} />
        </ExtensionPoint>
      </div>
    );
  }
}

export default withTranslation("repos")(DownloadViewer);
