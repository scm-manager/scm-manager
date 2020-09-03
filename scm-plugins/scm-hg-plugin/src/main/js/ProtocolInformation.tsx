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
import { Repository } from "@scm-manager/ui-types";
import { repositories } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
};

class ProtocolInformation extends React.Component<Props> {
  render() {
    const { repository, t } = this.props;
    const href = repositories.getProtocolLinkByType(repository, "http");
    if (!href) {
      return null;
    }
    return (
      <div>
        <h4>{t("scm-hg-plugin.information.clone")}</h4>
        <pre>
          <code>hg clone {href}</code>
        </pre>
        <h4>{t("scm-hg-plugin.information.create")}</h4>
        <pre>
          <code>
            hg init {repository.name}
            <br />
            cd {repository.name}
            <br />
            echo "[paths]" &gt; .hg/hgrc
            <br />
            echo "default = {href}
            " &gt;&gt; .hg/hgrc
            <br />
            echo "# {repository.name}
            " &gt; README.md
            <br />
            hg add README.md
            <br />
            hg commit -m "added readme"
            <br />
            <br />
            hg push
            <br />
          </code>
        </pre>
        <h4>{t("scm-hg-plugin.information.replace")}</h4>
        <pre>
          <code>
            # add the repository url as default to your .hg/hgrc e.g:
            <br />
            default = {href}
            <br />
            # push to remote repository
            <br />
            hg push
          </code>
        </pre>
      </div>
    );
  }
}

export default withTranslation("plugins")(ProtocolInformation);
