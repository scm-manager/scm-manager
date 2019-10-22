import { Link } from "react-router-dom";
import React from "react";
import { Changeset, Repository } from "@scm-manager/ui-types";
import { createChangesetLink } from "./changesets";

type Props = {
  repository: Repository;
  changeset: Changeset;
  link: boolean;
};

export default class ChangesetId extends React.Component<Props> {
  static defaultProps = {
    link: true
  };

  shortId = (changeset: Changeset) => {
    return changeset.id.substr(0, 7);
  };

  renderLink = () => {
    const { repository, changeset } = this.props;
    const link = createChangesetLink(repository, changeset);

    return <Link to={link}>{this.shortId(changeset)}</Link>;
  };

  renderText = () => {
    const { changeset } = this.props;
    return this.shortId(changeset);
  };

  render() {
    const { link } = this.props;
    if (link) {
      return this.renderLink();
    }
    return this.renderText();
  }
}
