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
import { apiClient } from "../apiclient";
import ErrorNotification from "../ErrorNotification";
// @ts-ignore
import parser from "gitdiff-parser";

import Loading from "../Loading";
import Diff from "./Diff";
import { DiffObjectProps, File } from "./DiffTypes";
import { NotFoundError } from "../errors";
import { Notification } from "../index";
import { withTranslation, WithTranslation } from "react-i18next";

type Props = WithTranslation &
  DiffObjectProps & {
    url: string;
  };

type State = {
  diff?: File[];
  loading: boolean;
  error?: Error;
};

class LoadingDiff extends React.Component<Props, State> {
  static defaultProps = {
    sideBySide: false
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      loading: true
    };
  }

  componentDidMount() {
    this.fetchDiff();
  }

  componentDidUpdate(prevProps: Props) {
    if (prevProps.url !== this.props.url) {
      this.fetchDiff();
    }
  }

  fetchDiff = () => {
    const { url } = this.props;
    this.setState({ loading: true });
    apiClient
      .get(url)
      .then(response => {
        const contentType = response.headers.get("Content-Type");
        if (contentType && contentType.toLowerCase() === "application/vnd.scmm-diffparsed+json;v=2") {
          return response.json().then(data => data.files);
        } else {
          return response.text().then(parser.parse);
        }
      })
      .then((diff: File[]) => {
        this.setState({
          loading: false,
          diff: diff
        });
      })
      .catch((error: Error) => {
        this.setState({
          loading: false,
          error
        });
      });
  };

  render() {
    const { diff, loading, error } = this.state;
    if (error) {
      if (error instanceof NotFoundError) {
        return <Notification type="info">{this.props.t("changesets.noChangesets")}</Notification>;
      }
      return <ErrorNotification error={error} />;
    } else if (loading) {
      return <Loading />;
    } else if (!diff) {
      return null;
    } else {
      return <Diff diff={diff} {...this.props} />;
    }
  }
}

export default withTranslation("repos")(LoadingDiff);
