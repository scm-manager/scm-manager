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
import { DateFromNow, InfoTable, MailLink } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
};

class RepositoryDetailTable extends React.Component<Props> {
  render() {
    const { repository, t } = this.props;
    return (
      <InfoTable>
        <tbody>
          <tr>
            <th>{t("repository.name")}</th>
            <td>{repository.name}</td>
          </tr>
          <tr>
            <th>{t("repository.type")}</th>
            <td>{repository.type}</td>
          </tr>
          <tr>
            <th>{t("repository.contact")}</th>
            <td>
              <MailLink address={repository.contact} />
            </td>
          </tr>
          <tr>
            <th>{t("repository.description")}</th>
            <td className="is-word-break">{repository.description}</td>
          </tr>
          <tr>
            <th>{t("repository.creationDate")}</th>
            <td>
              <DateFromNow date={repository.creationDate} />
            </td>
          </tr>
          <tr>
            <th>{t("repository.lastModified")}</th>
            <td>
              <DateFromNow date={repository.lastModified} />
            </td>
          </tr>
        </tbody>
      </InfoTable>
    );
  }
}

export default withTranslation("repos")(RepositoryDetailTable);
