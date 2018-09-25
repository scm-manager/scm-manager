//@flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import { MailLink, DateFromNow } from "@scm-manager/ui-components";
import ChangesetAvatar from "./ChangesetAvatar";

type Props = {
  changeset: Changeset,
  t: string => string
};

class ChangesetDetails extends React.Component<Props> {
  render() {
    const { changeset, t } = this.props;
    return (
      <div>
        <figure className="media-left">
          <ChangesetAvatar changeset={changeset} />
        </figure>
        <table className="table">
          <tbody>
            <tr>
              <td>{t("changeset.id")}</td>
              <td>{changeset.id}</td>
            </tr>
            <tr>
              <td>{t("author.name")}</td>
              <td>{changeset.author.name}</td>
            </tr>
            <tr>
              <td>{t("author.mail")}</td>
              <td>
                <MailLink address={changeset.author.mail} />
              </td>
            </tr>
            <tr>
              <td>{t("changeset.description")}</td>
              <td>{changeset.description}</td>
            </tr>
            <tr>
              <td>{t("changeset.date")}</td>
              <td>
                <DateFromNow date={changeset.date} />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    );
  }
}

export default translate("changesets")(ChangesetDetails);
