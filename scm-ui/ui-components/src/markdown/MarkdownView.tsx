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
import unified from "unified";
import parseMarkdown from "remark-parse";
import sanitize from "rehype-sanitize";
import remark2rehype from "remark-rehype";
import rehype2react from "rehype-react";
import gfm from "remark-gfm";
import { binder } from "@scm-manager/ui-extensions";
import ErrorBoundary from "../ErrorBoundary";
import { create as createMarkdownHeadingRenderer } from "./MarkdownHeadingRenderer";
import { create as createMarkdownLinkRenderer } from "./MarkdownLinkRenderer";
import { useTranslation, WithTranslation, withTranslation } from "react-i18next";
import Notification from "../Notification";
import { createTransformer } from "./remarkChangesetShortLinkParser";
import MarkdownCodeRenderer from "./MarkdownCodeRenderer";
import { AstPlugin } from "./PluginApi";
import createMdastPlugin from "./createMdastPlugin";
import gh from "hast-util-sanitize/lib/github";
import raw from "rehype-raw";
import slug from "rehype-slug";
import merge from "deepmerge";

type Props = RouteComponentProps &
  WithTranslation & {
    content: string;
    renderContext?: object;
    renderers?: any;
    skipHtml?: boolean;
    enableAnchorHeadings?: boolean;
    // basePath for markdown links
    basePath?: string;
    permalink?: string;
    mdastPlugins?: AstPlugin[];
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
      const element = contentRef.querySelector(`[id="${hash.substring(1)}"]`);
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
      skipHtml,
      basePath,
      permalink,
      t,
      mdastPlugins = []
    } = this.props;

    const rendererFactory = binder.getExtension("markdown-renderer-factory");
    let rendererList = renderers;

    if (rendererFactory) {
      rendererList = rendererFactory(renderContext);
    }

    if (!rendererList) {
      rendererList = {};
    }

    if (enableAnchorHeadings && permalink && !rendererList.heading) {
      rendererList.heading = createMarkdownHeadingRenderer(permalink);
    }

    if (basePath && !rendererList.link) {
      rendererList.link = createMarkdownLinkRenderer(basePath);
    }

    if (!rendererList.code) {
      rendererList.code = MarkdownCodeRenderer;
    }

    // NEW IMPL START

    const components: { [key: string]: any } = {};

    if (rendererList.code) {
      const codeRendererAdapter = function({ node, children }: any) {
        children = children || [];
        const renderProps = {
          value: children[0],
          language: Array.isArray(node.properties.className) ? node.properties.className[0].split("language-")[1] : ""
        };
        return React.createElement(rendererList.code, renderProps, ...children);
      };
      components.code = codeRendererAdapter;
    }

    if (rendererList.link) {
      const linkRendererAdapter = function({ node, children }: any) {
        const renderProps = {
          href: node.properties.href || ""
        };
        children = children || [];
        return React.createElement(rendererList.link, renderProps, ...children);
      };
      components.a = linkRendererAdapter;
    }

    if (rendererList.heading) {
      const createHeadingRendererAdapter = (level: number) => ({ node, children }: any) => {
        const renderProps = {
          id: node.properties.id,
          level,
          permalink
        };
        children = children || [];
        return React.createElement(rendererList.heading, renderProps, ...children);
      };
      components.h1 = createHeadingRendererAdapter(1);
      components.h2 = createHeadingRendererAdapter(2);
      components.h3 = createHeadingRendererAdapter(3);
      components.h4 = createHeadingRendererAdapter(4);
      components.h5 = createHeadingRendererAdapter(5);
      components.h6 = createHeadingRendererAdapter(6);
    }

    if (rendererList.break) {
      components.br = rendererList.break;
    }

    if (rendererList.delete) {
      components.del = rendererList.delete;
    }

    if (rendererList.emphasis) {
      components.em = rendererList.emphasis;
    }

    if (rendererList.blockquote) {
      components.blockquote = rendererList.blockquote;
    }

    if (rendererList.image) {
      components.img = rendererList.image;
    }

    if (rendererList.list) {
      components.ol = rendererList.list;
      components.ul = rendererList.list;
    }

    if (rendererList.listItem) {
      components.li = rendererList.listItem;
    }

    if (rendererList.paragraph) {
      components.p = rendererList.paragraph;
    }

    if (rendererList.strong) {
      components.strong = rendererList.strong;
    }

    if (rendererList.table) {
      components.table = rendererList.table;
    }

    if (rendererList.tableHead) {
      components.thead = rendererList.tableHead;
    }

    if (rendererList.tableBody) {
      components.tbody = rendererList.tableBody;
    }

    if (rendererList.tableRow) {
      components.tr = rendererList.tableRow;
    }

    if (rendererList.tableCell) {
      components.td = rendererList.tableCell;
      components.th = rendererList.tableCell;
    }

    if (rendererList.thematicBreak) {
      components.hr = rendererList.thematicBreak;
    }

    const plugins = [...mdastPlugins, createTransformer(t)].map(createMdastPlugin);

    let processor = unified()
      .use(parseMarkdown)
      .use(gfm)
      .use(plugins)
      .use(remark2rehype, { allowDangerousHtml: true });

    if (!skipHtml) {
      processor = processor.use(raw);
    }

    const schema: any = merge(gh, {
      attributes: {
        code: ["className"]
      },
      clobberPrefix: ""
    });

    processor = processor.use(slug).use(sanitize, schema);

    const toReactProcessor = processor.use(rehype2react, {
      createElement: React.createElement,
      passNode: true,
      components: components
    });

    return (
      <ErrorBoundary fallback={MarkdownErrorNotification}>
        <div ref={el => this.setState({ contentRef: el })} className="content is-word-break">
          // @ts-ignore
          {toReactProcessor.processSync(content).result}
        </div>
      </ErrorBoundary>
    );
  }
}

export default withRouter(withTranslation("repos")(MarkdownView));
