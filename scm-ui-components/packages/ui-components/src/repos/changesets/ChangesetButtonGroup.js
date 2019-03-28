//@flow
import React from "react";
import type { Changeset, Repository } from "@scm-manager/ui-types";
import ButtonGroup from "../../buttons/ButtonGroup";
import Button from "../../buttons/Button";
import { createChangesetLink, createSourcesLink } from "./changesets";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  changeset: Changeset,

  // context props
  t: (string) => string
}

class ChangesetButtonGroup extends React.Component<Props> {

  render() {
    const { repository, changeset, t } = this.props;

    const changesetLink = createChangesetLink(repository, changeset);
    const sourcesLink = createSourcesLink(repository, changeset);

    return (
      <ButtonGroup className="is-pulled-right">
        <Button link={changesetLink}>
          <span className="icon">
            <i className="fas fa-exchange-alt"></i>
          </span>
          <span className="is-hidden-mobile is-hidden-tablet-only">
            {t("changeset.buttons.details")}
          </span>
        </Button>
        <Button link={sourcesLink}>
          <span className="icon">
            <i className="fas fa-code"></i>
          </span>
          <span className="is-hidden-mobile is-hidden-tablet-only">
            {t("changeset.buttons.sources")}
          </span>
        </Button>
      </ButtonGroup>
    );
  }

}

export default translate("repos")(ChangesetButtonGroup);
