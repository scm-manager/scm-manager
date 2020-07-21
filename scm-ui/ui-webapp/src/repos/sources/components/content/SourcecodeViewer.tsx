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
import { WithTranslation, withTranslation } from "react-i18next";
import { apiClient, ErrorNotification, Loading, LineNumbers } from "@scm-manager/ui-components";
import { File, Link } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  file: File;
  language: string;
};

type State = {
  content: string;
  error?: Error;
  loaded: boolean;
  currentFileRevision: string;
};

class SourcecodeViewer extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      content: "",
      loaded: false,
      currentFileRevision: ""
    };
  }

  componentDidMount() {
    this.fetchContentIfChanged();
  }

  componentDidUpdate() {
    this.fetchContentIfChanged();
  }

  private fetchContentIfChanged() {
    const { file } = this.props;
    const { currentFileRevision } = this.state;
    if (file.revision !== currentFileRevision) {
      this.fetchContent();
    }
  }

  fetchContent = () => {
    const { file } = this.props;
    getContent((file._links.self as Link).href)
      .then(content => {
        this.setState({
          content,
          loaded: true,
          currentFileRevision: file.revision
        });
      })
      .catch(error => {
        this.setState({
          error,
          loaded: true
        });
      });
  };

  render() {
    const { content, error, loaded } = this.state;
    const language = this.props.language;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (!loaded) {
      return <Loading />;
    }

    if (!content) {
      return null;
    }
    return <LineNumbers language={getLanguage(language)} value={content} />;
  }
}

export function getLanguage(language: string) {
  return language.toLowerCase();
}

export function getContent(url: string) {
  return apiClient.get(url).then(response => response.text());
}

export default withTranslation("repos")(SourcecodeViewer);
