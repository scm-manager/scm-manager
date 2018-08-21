//@flow
import React from 'react';

// TODO flow types ???
type Props = {
  repository: Object
}

class CloneInformation extends React.Component<Props> {

  render() {
    const { repository } = this.repository;
    if (repository.type !== "git") {
      return null;
    }
    if (!repository._links.httpProtocol) {
      return null;
    }
    return (
      <div>
        <h2>Git</h2>
        <pre><code>
          git clone { repository._links.httpProtocol }
        </code></pre>
      </div>
    );
  }

}

export default CloneInformation;
