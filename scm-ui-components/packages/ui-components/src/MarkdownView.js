//@flow
import React from "react";
import SyntaxHighlighter from "./SyntaxHighlighter";
import Markdown from "react-markdown/with-html";
import {binder} from "@scm-manager/ui-extensions";
import MarkdownHeadingRenderer from "./MarkdownHeadingRenderer";

type Props = {
  content: string,
  renderContext?: Object,
  renderers?: Object,
  enableAnchorHeadings: boolean
};

class MarkdownView extends React.Component<Props> {

  static defaultProps = {
    enableAnchorHeadings: false
  };

  constructor(props: Props) {
    super(props);
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
      <Markdown
        className="content"
        skipHtml={true}
        escapeHtml={true}
        source={content}
        renderers={rendererList}
      />
    );
  }
}

export default MarkdownView;
