//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository
}

class ProtocolInformation extends React.Component<Props> {

  render() {
    const { repository } = this.props;
    if (!repository._links.httpProtocol) {
      return null;
    }
    return (
      <div>
        <h4>Clone the repository</h4>
        <pre>
          <code>git clone {repository._links.httpProtocol.href}</code>
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
            git remote add origin {repository._links.httpProtocol.href}
            <br />
            git push -u origin master
            <br />
          </code>
        </pre>
        <h4>Push an existing repository</h4>
        <pre>
          <code>
            git remote add origin {repository._links.httpProtocol.href}
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
