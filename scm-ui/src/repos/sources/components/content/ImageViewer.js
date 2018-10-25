// @flow
import React from "react";
import { translate } from "react-i18next";

type Props = {
  t: string => string
};

type State = {
  content: string
};

class ImageViewer extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      content: ""
    };
  }

  componentDidMount() {}

  render() {
    return "ImageViewer";
  }
}

export default translate("repos")(ImageViewer);
