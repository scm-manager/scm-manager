import React from "react";
import { Changeset, Repository } from "@scm-manager/ui-types";
import { ButtonAddons, Button } from "../../buttons";
import { createChangesetLink, createSourcesLink } from "./changesets";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  repository: Repository;
  changeset: Changeset;
};

class ChangesetButtonGroup extends React.Component<Props> {
  render() {
    const { repository, changeset, t } = this.props;
    const changesetLink = createChangesetLink(repository, changeset);
    const sourcesLink = createSourcesLink(repository, changeset);
    return (
      <ButtonAddons className="is-marginless">
        <Button link={changesetLink} icon="exchange-alt" label={t("changeset.buttons.details")} reducedMobile={true} />
        <Button link={sourcesLink} icon="code" label={t("changeset.buttons.sources")} reducedMobile={true} />
      </ButtonAddons>
    );
  }
}

export default withTranslation("repos")(ChangesetButtonGroup);
