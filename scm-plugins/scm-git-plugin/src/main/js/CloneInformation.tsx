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

type Props = WithTranslation & {
  url: string;
  repository: Repository;
};

class CloneInformation extends React.Component<Props> {
  render() {
    const { url, repository, t } = this.props;

    return (
      <div>
        <h4>{t("scm-git-plugin.information.clone")}</h4>
        <pre>
          <code>git clone {url}</code>
        </pre>
        <h4>{t("scm-git-plugin.information.create")}</h4>
        <pre>
          <code>
            git init {repository.name}
            <br />
            cd {repository.name}
            <br />
            echo "# {repository.name}
            " &gt; README.md
            <br />
            git add README.md
            <br />
            git commit -m "Add readme"
            <br />
            git remote add origin {url}
            <br />
            git push -u origin master
            <br />
          </code>
        </pre>
        <h4>{t("scm-git-plugin.information.replace")}</h4>
        <pre>
          <code>
            git remote add origin {url}
            <br />
            git push -u origin master
            <br />
          </code>
        </pre>
      </div>
    );
  }
}

export default withTranslation("plugins")(CloneInformation);
