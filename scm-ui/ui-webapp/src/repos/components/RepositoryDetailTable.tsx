import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { DateFromNow, MailLink } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";

type Props = WithTranslation & {
  repository: Repository;
};

class RepositoryDetailTable extends React.Component<Props> {
  render() {
    const { repository, t } = this.props;
    return (
      <table className="table">
        <tbody>
          <tr>
            <th>{t("repository.name")}</th>
            <td>
              <ExtensionPoint name={"repository.details.beforeName"} props={{ repository }} /> {repository.name}
            </td>
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

export default withTranslation("repos")(RepositoryDetailTable);
