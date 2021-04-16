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
import {Node, Parent} from "unist";

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
      const element = contentRef.querySelector(hash);
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

    const components: {[key: string]: any} = {};

    if (rendererList.code) {
      const codeRenderer = function({ node, children }: any, props: any) {
        console.log("renderer:code/pre", arguments);
        return React.createElement(rendererList.code, props, ...children);
      };
      components.code = codeRenderer;
      components.pre = codeRenderer;
    }

    const codeLanguageAttacherPlugin: AstPlugin = ({ visit }) => {
      visit("code", (node: Node, index: number, parent?: Parent) => {
        if (node.data) {
          node.data.lang = node.lang;
        } else {
          node.data = {
            lang: node.lang
          }
        }
        console.log("remark:code", node, index, parent);
      });
    };

    const plugins = [...mdastPlugins, createTransformer(t), codeLanguageAttacherPlugin].map(createMdastPlugin);

    let processor = unified()
      .use(parseMarkdown)
      .use(gfm)
      .use(plugins)
      .use(remark2rehype, { allowDangerousHtml: true });

    if (!skipHtml) {
      processor = processor.use(raw);
    }

    processor = processor.use(slug).use(sanitize, gh);

    const toReactProcessor = processor.use(rehype2react, {
      createElement: React.createElement,
      passNode: true,
      components
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
