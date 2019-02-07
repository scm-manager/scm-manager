//@flow
import React from "react";
import type { Changeset, Repository } from "@scm-manager/ui-types";
import ButtonGroup from "../../buttons/ButtonGroup";
import Button from "../../buttons/Button";
import { createChangesetLink, createSourcesLink } from "./changesets";

type Props = {
  repository: Repository,
  changeset: Changeset
}

class ChangesetButtonGroup extends React.Component<Props> {

  render() {
    const { repository, changeset } = this.props;

    const changesetLink = createChangesetLink(repository, changeset);
    const sourcesLink = createSourcesLink(repository, changeset);

    return (
      <ButtonGroup className="is-pulled-right">
        <Button link={changesetLink}>
          <span className="icon">
            <i className="fas fa-code"></i>
          </span>
          <span className="is-hidden-mobile is-hidden-tablet-only">
            Details
          </span>
        </Button>
        <Button link={sourcesLink}>
          <span className="icon">
            <i className="fas fa-history"></i>
          </span>
          <span className="is-hidden-mobile is-hidden-tablet-only">
            Sources
          </span>
        </Button>
      </ButtonGroup>
    );
  }

}

export default ChangesetButtonGroup;
