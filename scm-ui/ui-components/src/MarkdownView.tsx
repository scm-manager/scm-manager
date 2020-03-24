import React from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
// @ts-ignore
import Markdown from "react-markdown/with-html";
import { binder } from "@scm-manager/ui-extensions";
import ErrorBoundary from "./ErrorBoundary";
import SyntaxHighlighter from "./SyntaxHighlighter";
import MarkdownHeadingRenderer from "./MarkdownHeadingRenderer";

type Props = RouteComponentProps & {
  content: string;
  renderContext?: object;
  renderers?: any;
  skipHtml?: boolean;
  enableAnchorHeadings?: boolean;
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
    const { content, renderers, renderContext, enableAnchorHeadings, skipHtml } = this.props;

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

    if (!rendererList.code) {
      rendererList.code = SyntaxHighlighter;
    }

    return (
      <ErrorBoundary>
        <div ref={el => (this.contentRef = el)}>
          <Markdown
            className="content"
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
