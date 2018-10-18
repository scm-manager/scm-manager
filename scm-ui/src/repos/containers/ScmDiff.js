// @flow

import React from "react";
import { Diff, Hunk, parseDiff } from "react-diff-view";
import { apiClient } from "@scm-manager/ui-components";
import type { Repository } from "@scm-manager/ui-types";
import { Diff2Html } from "diff2html";

type Props = {
  //TODO: Actually, we want the Changeset here
  repository: Repository,
  revision: string,
  sideBySide: boolean
};

type State = {
  diff: string,
  error?: Error
};

class ScmDiff extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { diff: "" };
  }

  componentDidMount() {
    const { repository, revision } = this.props;
    const { namespace, name } = repository;
    const url = `/repositories/${namespace}/${name}/diff/${revision}`; //TODO: use HATEOAS link from changeset
    apiClient
      .get(url)
      .then(response => response.text())
      .then(text => this.setState({ ...this.state, diff: text }))
      .catch(error => this.setState({ ...this.state, error }));
  }

  render() {
    const options = {
      inputFormat: "diff",
      outputFormat: this.props.sideBySide ? "side-by-side" : "line-by-line",
      showFiles: true,
      matching: "lines"
    };

    const outputHtml = Diff2Html.getPrettyHtml(this.state.diff, options);

    return (
      // eslint-disable-next-line react/no-danger
      <div dangerouslySetInnerHTML={{ __html: outputHtml }} />
    );
    // if (!this.state.diff) {
    //   return null;
    // }
    //
    // const files = parseDiff(this.state.diff);
    //
    // const renderFile = ({ newPath, oldRevision, newRevision, type, hunks }) => (
    //   <div>
    //     <div> File: {newPath} </div>
    //     <Diff
    //       key={oldRevision + "-" + newRevision}
    //       diffType={type}
    //       hunks={hunks}
    //       viewType="unified"
    //     />
    //   </div>
    // );
    //
    // return <div>{files.map(renderFile)}</div>;
  }
}

export default ScmDiff;
