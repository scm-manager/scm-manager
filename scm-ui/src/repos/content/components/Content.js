// @flow
import React from "react";
import { translate } from "react-i18next";
import { apiClient } from "@scm-manager/ui-components";

type Props = {
  t: string => string
};

type State = {
  contentType: string
};

class Content extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      contentType: ""
    };
  }

  componentDidMount() {}

  render() {
    return "Hallo here is content";
  }
}

export function getContentType(url: string) {
  return apiClient
    .head(url)
    .then(response => response.headers.get("Content-Type"))
    .catch(err => {
      return null;
    });
}

export default translate("repos")(Content);
