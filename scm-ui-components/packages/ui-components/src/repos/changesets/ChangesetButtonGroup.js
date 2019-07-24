//@flow
import React from "react";
import type { Changeset, Repository } from "@scm-manager/ui-types";
import { ButtonAddons, Button } from "../../buttons";
import { createChangesetLink, createSourcesLink } from "./changesets";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  changeset: Changeset,

  // context props
  t: string => string
};

class ChangesetButtonGroup extends React.Component<Props> {
  render() {
    const { repository, changeset, t } = this.props;

    const changesetLink = createChangesetLink(repository, changeset);
    const sourcesLink = createSourcesLink(repository, changeset);

    return (
      <ButtonAddons className="level-item">
        <Button link={changesetLink} className="reduced-mobile">
          <span className="icon">
            <i className="fas fa-exchange-alt" />
          </span>
          <span>{t("changeset.buttons.details")}</span>
        </Button>
        <Button link={sourcesLink} className="reduced-mobile">
          <span className="icon">
            <i className="fas fa-code" />
          </span>
          <span>{t("changeset.buttons.sources")}</span>
        </Button>
      </ButtonAddons>
    );
  }
}

export default translate("repos")(ChangesetButtonGroup);
