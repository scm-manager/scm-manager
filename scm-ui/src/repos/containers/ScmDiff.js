// @flow

import React from "react";
import {Diff, Hunk, parseDiff} from "react-diff-view";
import {apiClient} from "@scm-manager/ui-components";

type Props = {
  namespace: string,
  name: string,
  revision: string
};

class ScmDiff extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const {namespace, name, revision} = this.props;
    const url = `/repositories/${namespace}/${name}/diff/${revision}`;
    apiClient
      .get(url)
      .then(response => response.text())
      .then(text => this.setState({diff: text}))
      .catch(error => this.setState({error}));
  }

  render() {
    if (!this.state.diff || this.state.diff === "") {
      return null;
    }
    const files = parseDiff(this.state.diff);

    const renderFile = ({newPath, oldRevision, newRevision, type, hunks}) => (
      <div>
        <div> File: {newPath} </div>
        <Diff key={oldRevision + '-' + newRevision}  diffType={type} hunks={hunks} viewType="split">
        </Diff>
      </div>
    );

    return (
      <div>
        {files.map(renderFile)}
      </div>
    );
  }
}

export default ScmDiff;
