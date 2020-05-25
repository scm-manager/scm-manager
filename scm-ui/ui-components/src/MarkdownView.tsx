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
import { withRouter, RouteComponentProps } from "react-router-dom";
// @ts-ignore
import Markdown from "react-markdown/with-html";
import { binder } from "@scm-manager/ui-extensions";
import ErrorBoundary from "./ErrorBoundary";
import SyntaxHighlighter from "./SyntaxHighlighter";
import MarkdownHeadingRenderer from "./MarkdownHeadingRenderer";
import { create } from "./MarkdownLinkRenderer";
import { useTranslation } from "react-i18next";
import Notification from "./Notification";

type Props = RouteComponentProps & {
  content: string;
  renderContext?: object;
  renderers?: any;
  skipHtml?: boolean;
  enableAnchorHeadings?: boolean;
  // basePath for markdown links
  basePath?: string;
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

class MarkdownView extends React.Component<Props> {
  static defaultProps: Partial<Props> = {
    enableAnchorHeadings: false,
    skipHtml: false
  };

  contentRef: HTMLDivElement | null | undefined;

  constructor(props: Props) {
    super(props);
  }

  componentDidUpdate() {
    // we have to use componentDidUpdate, because we have to wait until all
    // children are rendered and componentDidMount is called before the
    // markdown content was rendered.
    const hash = this.props.location.hash;
    if (this.contentRef && hash) {
      // we query only child elements, to avoid strange scrolling with multiple
      // markdown elements on one page.
      const element = this.contentRef.querySelector(hash);
      if (element && element.scrollIntoView) {
        element.scrollIntoView();
      }
    }
  }

  render() {
    const { content, renderers, renderContext, enableAnchorHeadings, skipHtml, basePath } = this.props;

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
        <div ref={el => (this.contentRef = el)}>
          <Markdown
            className="content is-word-break"
            skipHtml={skipHtml}
            escapeHtml={skipHtml}
            source={content}
            renderers={rendererList}
          />
        </div>
      </ErrorBoundary>
    );
  }
}

export default withRouter(MarkdownView);
