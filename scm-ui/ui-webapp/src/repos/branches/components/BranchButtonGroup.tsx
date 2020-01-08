import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Repository } from "@scm-manager/ui-types";
import { Button, ButtonAddons } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
};

class BranchButtonGroup extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    const changesetLink = `/repo/${repository.namespace}/${repository.name}/branch/${encodeURIComponent(
      branch.name
    )}/changesets/`;
    const sourcesLink = `/repo/${repository.namespace}/${repository.name}/sources/${encodeURIComponent(branch.name)}/`;

    return (
      <ButtonAddons>
        <Button link={changesetLink} icon="exchange-alt" label={t("branch.commits")} reducedMobile={true} />
        <Button link={sourcesLink} icon="code" label={t("branch.sources")} reducedMobile={true} />
      </ButtonAddons>
    );
  }
}

export default withTranslation("repos")(BranchButtonGroup);
