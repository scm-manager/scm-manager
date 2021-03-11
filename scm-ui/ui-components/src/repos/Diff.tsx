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
import DiffFile from "./DiffFile";
import { DiffObjectProps, FileControlFactory } from "./DiffTypes";
import { FileDiff } from "@scm-manager/ui-types";
import { escapeWhitespace } from "./diffs";
import Notification from "../Notification";
import { WithTranslation, withTranslation } from "react-i18next";
import { RouteComponentProps, withRouter } from "react-router-dom";

type Props = RouteComponentProps &
  WithTranslation &
  DiffObjectProps & {
    diff: FileDiff[];
    fileControlFactory?: FileControlFactory;
  };

type State = {
  contentRef?: HTMLElement | null;
};

function getAnchorSelector(uriHashContent: string) {
  return "#" + escapeWhitespace(decodeURIComponent(uriHashContent));
}

class Diff extends React.Component<Props, State> {
  static defaultProps: Partial<Props> = {
    sideBySide: false
  };

  constructor(props: Readonly<Props>) {
    super(props);
    this.state = {
      contentRef: undefined
    };
  }

  componentDidUpdate() {
    const { contentRef } = this.state;

    // we have to use componentDidUpdate, because we have to wait until all
    // children are rendered and componentDidMount is called before the
    // changeset content was rendered.
    const hash = this.props.location.hash;
    const match = hash && hash.match(/^#diff-(.*)$/);
    if (contentRef && match) {
      const selector = getAnchorSelector(match[1]);
      const element = contentRef.querySelector(selector);
      if (element && element.scrollIntoView) {
        element.scrollIntoView();
      }
    }
  }

  shouldComponentUpdate(nextProps: Readonly<Props>, nextState: Readonly<State>): boolean {
    // We have check if the contentRef changed and update afterwards so the page can scroll to the anchor links.
    // Otherwise it can happen that componentDidUpdate is never executed depending on how fast the markdown got rendered
    return this.state.contentRef !== nextState.contentRef || this.props !== nextProps;
  }

  render() {
    const { diff, t, ...fileProps } = this.props;

    return (
      <div ref={el => this.setState({ contentRef: el })}>
        {diff.length === 0 ? (
          <Notification type="info">{t("diff.noDiffFound")}</Notification>
        ) : (
          diff.map((file, index) => <DiffFile key={index} file={file} {...fileProps} {...this.props} />)
        )}
      </div>
    );
  }
}

export default withRouter(withTranslation("repos")(Diff));
