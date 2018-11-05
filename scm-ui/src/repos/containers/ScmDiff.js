// @flow

import React from "react";
import { apiClient } from "@scm-manager/ui-components";
import type { Changeset } from "@scm-manager/ui-types";
import { Diff2Html } from "diff2html";

type Props = {
  changeset: Changeset,
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
    const { changeset } = this.props;
    const url = changeset._links.diff.href+"?format=GIT";
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
      showFiles: false,
      matching: "lines"
    };

    const outputHtml = Diff2Html.getPrettyHtml(this.state.diff, options);

    return (
      // eslint-disable-next-line react/no-danger
      <div dangerouslySetInnerHTML={{ __html: outputHtml }} />
    );
  }
}

export default ScmDiff;
