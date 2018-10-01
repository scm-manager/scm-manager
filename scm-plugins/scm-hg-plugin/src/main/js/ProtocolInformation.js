//@flow
import React from "react";
import { repositories } from "@scm-manager/ui-components";
import type { Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository
}

class ProtocolInformation extends React.Component<Props> {

  render() {
    const { repository } = this.props;
    const href = repositories.getProtocolLinkByType(repository, "http");
    if (!href) {
      return null;
    }
    return (
      <div>
        <h4>Clone the repository</h4>
        <pre>
          <code>hg clone {href}</code>
        </pre>
        <h4>Create a new repository</h4>
        <pre>
          <code>
            hg init {repository.name}
            <br />
            echo "[paths]" > .hg/hgrc
            <br />
            echo "default = {href}" > .hg/hgrc
            <br />
            echo "# {repository.name}" > README.md
            <br />
            hg add README.md
            <br />
            hg commit -m "added readme"
            <br />
            <br />
            hg push
            <br />
          </code>
        </pre>
        <h4>Push an existing repository</h4>
        <pre>
          <code>
            # add the repository url as default to your .hg/hgrc e.g:
            <br />
            default = {href}
            <br />
            # push to remote repository
            <br />
            hg push
          </code>
        </pre>
      </div>
    );
  }

}

export default ProtocolInformation;
