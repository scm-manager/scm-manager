//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { MailLink, DateFromNow } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  // context props
  t: string => string
};

class RepositoryDetailTable extends React.Component<Props> {
  render() {
    const { repository, t } = this.props;
    return (
      <table className="table">
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
            <td>{repository.description}</td>
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
      </table>
    );
  }
}

export default translate("repos")(RepositoryDetailTable);
