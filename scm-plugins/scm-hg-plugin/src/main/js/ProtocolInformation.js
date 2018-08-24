//@flow
import React from 'react';

// TODO flow types ???
type Props = {
  repository: Object
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
          <code>hg clone {repository._links.httpProtocol.href}</code>
        </pre>
        <h4>Create a new repository</h4>
        <pre>
          <code>
            hg init {repository.name}
            <br />
            echo "[paths]" > .hg/hgrc
            <br />
            echo "default = {repository._links.httpProtocol.href}" > .hg/hgrc
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
            default = {repository._links.httpProtocol.href}
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
