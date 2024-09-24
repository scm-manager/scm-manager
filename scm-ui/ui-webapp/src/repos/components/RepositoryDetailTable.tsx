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

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { DateFromNow, InfoTable, MailLink } from "@scm-manager/ui-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";

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
        <ExtensionPoint<extensionPoints.RepositoryInformationTableBottom> name="repository.information.table.bottom" props={{repository}} renderAll={true} />
        </tbody>
      </InfoTable>
    );
  }
}

export default withTranslation("repos")(RepositoryDetailTable);
