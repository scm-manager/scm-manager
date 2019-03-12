//@flow
import React from "react";
import SyntaxHighlighter from "./SyntaxHighlighter";
import Markdown from "react-markdown/with-html";
import {binder} from "@scm-manager/ui-extensions";

type Props = {
  content: string,
  className : string,
  renderContext? : Object,
  renderers?: Object,
};

class MarkdownView extends React.Component<Props> {

  render() {
    const {content, className, renderers, renderContext} = this.props;

    const rendererFactory = binder.getExtension("markdown-renderer-factory");
    let rendererList = renderers;

    if (rendererFactory){
      rendererList = rendererFactory(renderContext);
    }

    if (!rendererList){
      rendererList = {};
    }

    if (!rendererList.code){
      rendererList.code = SyntaxHighlighter;
    }

    return (
      <Markdown
        className={className}
        skipHtml={true}
        escapeHtml={true}
        source={content}
        renderers={rendererList}
      />
    );
  }
}

export default MarkdownView;
