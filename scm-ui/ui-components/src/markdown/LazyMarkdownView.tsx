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
import { BinderContext } from "@scm-manager/ui-extensions";
import ErrorBoundary from "../ErrorBoundary";
import { create as createMarkdownHeadingRenderer } from "./MarkdownHeadingRenderer";
import { create as createMarkdownLinkRenderer } from "./MarkdownLinkRenderer";
import { useTranslation, WithTranslation, withTranslation } from "react-i18next";
import Notification from "../Notification";
import { createTransformer as createChangesetShortlinkParser } from "./remarkChangesetShortLinkParser";
import { createTransformer as createValuelessTextAdapter } from "./remarkValuelessTextAdapter";
import MarkdownCodeRenderer from "./MarkdownCodeRenderer";
import { AstPlugin } from "./PluginApi";
import createMdastPlugin from "./createMdastPlugin";
// @ts-ignore
import gh from "hast-util-sanitize/lib/github";
import raw from "rehype-raw";
import slug from "rehype-slug";
import merge from "deepmerge";
import { createComponentList } from "./createComponentList";
import { ProtocolLinkRendererExtension, ProtocolLinkRendererExtensionMap } from "./markdownExtensions";

export type MarkdownProps = {
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

type Props = RouteComponentProps & WithTranslation & MarkdownProps;

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
          <a href="https://github.github.com/gfm/" target="_blank" rel="noreferrer">
            GitHub Flavored Markdown Spec
          </a>
        </p>
      </div>
    </Notification>
  );
};

class LazyMarkdownView extends React.Component<Props, State> {
  static contextType = BinderContext;

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
      const elementId = decodeURIComponent(hash.substring(1) /* remove # */);
      const element = contentRef.querySelector(`[id="${elementId}"]`);
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

    const rendererFactory = this.context.getExtension("markdown-renderer-factory");
    let remarkRendererList = renderers;

    if (rendererFactory) {
      remarkRendererList = rendererFactory(renderContext);
    }

    if (!remarkRendererList) {
      remarkRendererList = {};
    }

    if (enableAnchorHeadings && permalink && !remarkRendererList.heading) {
      remarkRendererList.heading = createMarkdownHeadingRenderer(permalink);
    }

    let protocolLinkRendererExtensions: ProtocolLinkRendererExtensionMap = {};
    if (!remarkRendererList.link) {
      const extensionPoints = this.context.getExtensions(
        "markdown-renderer.link.protocol"
      ) as ProtocolLinkRendererExtension[];
      protocolLinkRendererExtensions = extensionPoints.reduce<ProtocolLinkRendererExtensionMap>(
        (prev, { protocol, renderer }) => {
          prev[protocol] = renderer;
          return prev;
        },
        {}
      );
      remarkRendererList.link = createMarkdownLinkRenderer(basePath, protocolLinkRendererExtensions);
    }

    if (!remarkRendererList.code) {
      remarkRendererList.code = MarkdownCodeRenderer;
    }

    const remarkPlugins = [...mdastPlugins, createChangesetShortlinkParser(t), createValuelessTextAdapter()].map(
      createMdastPlugin
    );

    let processor = unified()
      .use(parseMarkdown)
      .use(gfm)
      .use(remarkPlugins)
      .use(remark2rehype, { allowDangerousHtml: true });

    if (!skipHtml) {
      processor = processor.use(raw);
    }

    processor = processor
      .use(slug)
      .use(
        sanitize,
        merge(gh, {
          attributes: {
            code: ["className"] // Allow className for code elements, this is necessary to extract the code language
          },
          clobberPrefix: "", // Do not prefix user-provided ids and class names,
          protocols: {
            href: Object.keys(protocolLinkRendererExtensions)
          }
        })
      )
      .use(rehype2react, {
        createElement: React.createElement,
        passNode: true,
        components: createComponentList(remarkRendererList, { permalink })
      });

    const renderedMarkdown: any = processor.processSync(content).result;

    return (
      <ErrorBoundary fallback={MarkdownErrorNotification}>
        <div ref={el => this.setState({ contentRef: el })} className="content is-word-break">
          {renderedMarkdown}
        </div>
      </ErrorBoundary>
    );
  }
}

export default withRouter(withTranslation("repos")(LazyMarkdownView));
