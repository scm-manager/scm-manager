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
            <td className="has-text-weight-semibold">{t("repository.name")}</td>
            <td>{repository.name}</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("repository.type")}</td>
            <td>{repository.type}</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("repository.contact")}</td>
            <td>
              <MailLink address={repository.contact} />
            </td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("repository.description")}</td>
            <td>{repository.description}</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("repository.creationDate")}</td>
            <td>
              <DateFromNow date={repository.creationDate} />
            </td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("repository.lastModified")}</td>
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
