//@flow
import React from "react";
import { withRouter } from "react-router-dom";
import injectSheet from "react-jss";
import Markdown from "react-markdown/with-html";
import { binder } from "@scm-manager/ui-extensions";
import SyntaxHighlighter from "./SyntaxHighlighter";
import MarkdownHeadingRenderer from "./MarkdownHeadingRenderer";

type Props = {
  content: string,
  renderContext?: Object,
  renderers?: Object,
  enableAnchorHeadings: boolean,

  // context props
  classes: any,
  location: any
};

const styles = {
  markdown: {
    "& > .content": {
      "& > h1, h2, h3, h4, h5, h6": {
        margin: "0.5rem 0",
        fontSize: "0.9rem"
      },
      "& > h1": {
        fontWeight: "700"
      },
      "& > h2": {
        fontWeight: "600"
      },
      "& > h3, h4, h5, h6": {
        fontWeight: "500"
      },
      "& strong": {
        fontWeight: "500"
      }
    }
  }
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
    const {
      content,
      renderers,
      renderContext,
      enableAnchorHeadings,
      classes
    } = this.props;

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
      <div className={classes.markdown} ref={el => (this.contentRef = el)}>
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

export default injectSheet(styles)(withRouter(MarkdownView));
