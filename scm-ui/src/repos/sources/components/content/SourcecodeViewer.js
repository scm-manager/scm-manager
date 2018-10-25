// @flow
import React from "react";
import { translate } from "react-i18next";
import { apiClient } from "../../../../../../scm-ui-components/packages/ui-components/src/index";

type Props = {
  t: string => string
};

type State = {
  content: string
};

class SourcecodeViewer extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      content: ""
    };
  }

  componentDidMount() {}

  render() {
    return "sourceCodeViewer";
  }
}

export function getContent(url: string) {
  return apiClient
    .get(url)
    .then(response => response.text())
    .catch(err => {
      return null;
    });
}

export default translate("repos")(SourcecodeViewer);
