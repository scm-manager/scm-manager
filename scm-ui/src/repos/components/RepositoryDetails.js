//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Repository } from "../types/Repositories";
import MailLink from "../../components/MailLink";
import DateFromNow from "../../components/DateFromNow";

type Props = {
  repository: Repository,
  // context props
  t: string => string
};

class RepositoryDetails extends React.Component<Props> {
  render() {
    const { repository, t } = this.props;
    return (
      <table className="table">
        <tbody>
          <tr>
            <td>{t("repository.name")}</td>
            <td>{repository.name}</td>
          </tr>
          <tr>
            <td>{t("repository.type")}</td>
            <td>{repository.type}</td>
          </tr>
          <tr>
            <td>{t("repository.contact")}</td>
            <td>
              <MailLink address={repository.contact} />
            </td>
          </tr>
          <tr>
            <td>{t("repository.description")}</td>
            <td>{repository.description}</td>
          </tr>
          <tr>
            <td>{t("repository.creationDate")}</td>
            <td>
              <DateFromNow date={repository.creationDate} />
            </td>
          </tr>
          <tr>
            <td>{t("repository.lastModified")}</td>
            <td>
              <DateFromNow date={repository.lastModified} />
            </td>
          </tr>
        </tbody>
      </table>
    );
  }
}

export default translate("repos")(RepositoryDetails);
