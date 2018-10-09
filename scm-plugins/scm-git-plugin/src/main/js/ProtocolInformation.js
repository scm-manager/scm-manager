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
          <code>git clone {href}</code>
        </pre>
        <h4>Create a new repository</h4>
        <pre>
          <code>
            git init {repository.name}
            <br />
            echo "# {repository.name}" > README.md
            <br />
            git add README.md
            <br />
            git commit -m "added readme"
            <br />
            git remote add origin {href}
            <br />
            git push -u origin master
            <br />
          </code>
        </pre>
        <h4>Push an existing repository</h4>
        <pre>
          <code>
            git remote add origin {href}
            <br />
            git push -u origin master
            <br />
          </code>
        </pre>
      </div>
    );
  }

}

export default ProtocolInformation;
