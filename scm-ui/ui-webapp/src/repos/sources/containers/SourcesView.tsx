/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import SourcecodeViewer from "../components/content/SourcecodeViewer";
import ImageViewer from "../components/content/ImageViewer";
import DownloadViewer from "../components/content/DownloadViewer";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { getContentType } from "./contentType";
import { File, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import SwitchableMarkdownViewer from "../components/content/SwitchableMarkdownViewer";

type Props = {
  repository: Repository;
  file: File;
  revision: string;
  path: string;
};

type State = {
  contentType: string;
  language: string;
  loaded: boolean;
  error?: Error;
};

class SourcesView extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      contentType: "",
      language: "",
      loaded: false
    };
  }

  componentDidMount() {
    const { file } = this.props;
    getContentType(file._links.self.href)
      .then(result => {
        this.setState({
          ...this.state,
          contentType: result.type,
          language: result.language,
          loaded: true
        });
      })
      .catch(error => {
        this.setState({
          ...this.state,
          error,
          loaded: true
        });
      });
  }

  createBasePath() {
    const { repository, revision } = this.props;
    return `/repo/${repository.namespace}/${repository.name}/code/sources/${revision}/`;
  }

  showSources() {
    const { file, revision } = this.props;
    const { contentType, language } = this.state;
    const basePath = this.createBasePath();
    if (contentType.startsWith("image/")) {
      return <ImageViewer file={file} />;
    } else if (contentType.includes("markdown") || (language && language.toLowerCase() === "markdown")) {
      return <SwitchableMarkdownViewer file={file} basePath={basePath} />;
    } else if (language) {
      return <SourcecodeViewer file={file} language={language} />;
    } else if (contentType.startsWith("text/")) {
      return <SourcecodeViewer file={file} language="none" />;
    } else {
      return (
        <ExtensionPoint
          name="repos.sources.view"
          props={{
            file,
            contentType,
            revision,
            basePath
          }}
        >
          <DownloadViewer file={file} />
        </ExtensionPoint>
      );
    }
  }

  render() {
    const { file } = this.props;
    const { loaded, error } = this.state;

    if (!file || !loaded) {
      return <Loading />;
    }
    if (error) {
      return <ErrorNotification error={error} />;
    }

    const sources = this.showSources();

    return <div className="panel-block">{sources}</div>;
  }
}

export default SourcesView;
