//@flow
import React from "react";
import SyntaxHighlighter from "./SyntaxHighlighter";
import Markdown from "react-markdown/with-html";
import {binder} from "@scm-manager/ui-extensions";
import MarkdownHeadingRenderer from "./MarkdownHeadingRenderer";
import { withRouter } from "react-router-dom";


type Props = {
  content: string,
  renderContext?: Object,
  renderers?: Object,
  enableAnchorHeadings: boolean,

  // context props
  location: any
};

class MarkdownView extends React.Component<Props> {

  static defaultProps = {
    enableAnchorHeadings: false
  };

  contentRef: ?HTMLDivElement;

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
    const {content, renderers, renderContext, enableAnchorHeadings} = this.props;

    const rendererFactory = binder.getExtension("markdown-renderer-factory");
    let rendererList = renderers;

    if (rendererFactory){
      rendererList = rendererFactory(renderContext);
    }

    if (!rendererList){
      rendererList = {};
    }

    if (enableAnchorHeadings) {
      rendererList.heading = MarkdownHeadingRenderer;
    }

    if (!rendererList.code){
      rendererList.code = SyntaxHighlighter;
    }

    return (
      <div ref={el => (this.contentRef = el)}>
        <Markdown
          className="content"
          skipHtml={true}
          escapeHtml={true}
          source={content}
          renderers={rendererList}
        />
      </div>
    );
  }
}

export default withRouter(MarkdownView);
