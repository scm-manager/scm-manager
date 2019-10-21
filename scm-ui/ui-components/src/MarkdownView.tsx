import React from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
import Markdown from "react-markdown/with-html";
import styled from "styled-components";
import { binder } from "@scm-manager/ui-extensions";
import SyntaxHighlighter from "./SyntaxHighlighter";
import MarkdownHeadingRenderer from "./MarkdownHeadingRenderer";

type Props = RouteComponentProps & {
  content: string;
  renderContext?: object;
  renderers?: any;
  skipHtml?: boolean;
  enableAnchorHeadings?: boolean;
};

const MarkdownWrapper = styled.div`
  > .content {
    > h1,
    h2,
    h3,
    h4,
    h5,
    h6 {
      margin: 0.5rem 0;
      font-size: 0.9rem;
    }
    > h1 {
      font-weight: 700;
    }
    > h2 {
      font-weight: 600;
    }
    > h3,
    h4,
    h5,
    h6 {
      font-weight: 500;
    }
    & strong {
      font-weight: 500;
    }
  }
`;

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
      <MarkdownWrapper ref={el => (this.contentRef = el)}>
        <Markdown
          className="content"
          skipHtml={skipHtml}
          escapeHtml={skipHtml}
          source={content}
          renderers={rendererList}
        />
      </MarkdownWrapper>
    );
  }
}

export default withRouter(MarkdownView);
