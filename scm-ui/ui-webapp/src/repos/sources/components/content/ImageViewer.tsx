import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { File } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  file: File;
};

class ImageViewer extends React.Component<Props> {
  render() {
    const { file } = this.props;
    return (
      <div className="has-text-centered">
        <figure>
          <img src={file._links.self.href} alt={file._links.self.href} />
        </figure>
      </div>
    );
  }
}

export default withTranslation("repos")(ImageViewer);
