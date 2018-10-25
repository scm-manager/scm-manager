// @flow
import React from "react";
import { translate } from "react-i18next";
import type { File } from "@scm-manager/ui-types";
import { DownloadButton, DateFromNow } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  file: File,
  revision: string
};

class DownloadViewer extends React.Component<Props> {
  render() {
    const { t, file, revision } = this.props;
    return (
      <div>
        <article className="media">
          <div className="content media-left">
            <h4>{file.name}</h4>
          </div>
          <div className="media-content" />
          <div className="media-right">
            <DownloadButton
              url={file._links.self.href}
              displayName={t("sources.content.downloadButton")}
            />
          </div>
        </article>
        <table className="table">
          <tbody>
            <tr>
              <td>{t("sources.description")}</td>
              <td>{file.description}</td>
            </tr>
            <tr>
              <td>{t("sources.lastModified")}</td>
              <td>
                <DateFromNow date={file.lastModified} />
              </td>
            </tr>
            <tr>
              <td>{t("sources.branch")}</td>
              <td>{revision}</td>
            </tr>
          </tbody>
        </table>
      </div>
    );
  }
}

export default translate("repos")(DownloadViewer);
