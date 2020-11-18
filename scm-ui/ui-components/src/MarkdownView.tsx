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
import React, { FC } from "react";
import { RouteComponentProps, withRouter } from "react-router-dom";
// @ts-ignore
import Markdown from "react-markdown/with-html";
import { binder } from "@scm-manager/ui-extensions";
import ErrorBoundary from "./ErrorBoundary";
import SyntaxHighlighter from "./SyntaxHighlighter";
import MarkdownHeadingRenderer from "./MarkdownHeadingRenderer";
import { create } from "./MarkdownLinkRenderer";
import { useTranslation, WithTranslation, withTranslation } from "react-i18next";
import Notification from "./Notification";
import { createTransformer } from "./remarkChangesetShortLinkParser";

type Props = RouteComponentProps &
  WithTranslation & {
    content: string;
    renderContext?: object;
    renderers?: any;
    skipHtml?: boolean;
    enableAnchorHeadings?: boolean;
    // basePath for markdown links
    basePath?: string;
  };

type State = {
  contentRef: HTMLDivElement | null | undefined;
};

const xmlMarkupSample = `\`\`\`xml
<your>
  <xml>
    <content/>
  </xml>
</your>
\`\`\``;

const MarkdownErrorNotification: FC = () => {
  const [t] = useTranslation("commons");
  return (
    <Notification type="danger">
      <div className="content">
        <p className="subtitle">{t("markdownErrorNotification.title")}</p>
        <p>{t("markdownErrorNotification.description")}</p>
        <pre>
          <code>{xmlMarkupSample}</code>
        </pre>
        <p>
          {t("markdownErrorNotification.spec")}:{" "}
          <a href="https://github.github.com/gfm/" target="_blank">
            GitHub Flavored Markdown Spec
          </a>
        </p>
      </div>
    </Notification>
  );
};

class MarkdownView extends React.Component<Props, State> {
  static defaultProps: Partial<Props> = {
    enableAnchorHeadings: false,
    skipHtml: false
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      contentRef: null
    };
  }

  shouldComponentUpdate(nextProps: Readonly<Props>, nextState: Readonly<State>): boolean {
    // We have check if the contentRef changed and update afterwards so the page can scroll to the anchor links.
    // Otherwise it can happen that componentDidUpdate is never executed depending on how fast the markdown got rendered
    // We also have to check if props have changed, because we also want to rerender if one of our props has changed
    return this.state.contentRef !== nextState.contentRef || this.props !== nextProps;
  }

  componentDidUpdate() {
    const { contentRef } = this.state;
    // we have to use componentDidUpdate, because we have to wait until all
    // children are rendered and componentDidMount is called before the
    // markdown content was rendered.
    const hash = this.props.location.hash;
    if (contentRef && hash) {
      // we query only child elements, to avoid strange scrolling with multiple
      // markdown elements on one page.
      const element = contentRef.querySelector(hash);
      if (element && element.scrollIntoView) {
        element.scrollIntoView();
      }
    }
  }

  render() {
    const { content, renderers, renderContext, enableAnchorHeadings, skipHtml, basePath, t } = this.props;

    const rendererFactory = binder.getExtension("markdown-renderer-factory");
    let rendererList = renderers;

    if (rendererFactory) {
      rendererList = rendererFactory(renderContext);
    }

    if (!rendererList) {
      rendererList = {};
    }

    if (enableAnchorHeadings) {
      rendererList.heading = MarkdownHeadingRenderer;
    }

    if (basePath && !rendererList.link) {
      rendererList.link = create(basePath);
    }

    if (!rendererList.code) {
      rendererList.code = SyntaxHighlighter;
    }

    return (
      <ErrorBoundary fallback={MarkdownErrorNotification}>
        <div ref={el => this.setState({ contentRef: el })}>
          <Markdown
            className="content is-word-break"
            skipHtml={skipHtml}
            source={content}
            renderers={rendererList}
            astPlugins={[createTransformer(t)]}
          />
        </div>
      </ErrorBoundary>
    );
  }
}

export default withRouter(withTranslation("repos")(MarkdownView));
