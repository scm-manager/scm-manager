/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
import { create as createMarkdownImageRenderer } from "./MarkdownImageRenderer";
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
import styled from "styled-components";
import classNames from "classnames";

export type MarkdownProps = {
  content: string;
  className?: string;
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

const HorizontalScrollDiv = styled.div`
  overflow-x: auto;
  overflow-y: hidden;
`;

class LazyMarkdownView extends React.Component<Props, State> {
  static contextType = BinderContext;

  static defaultProps: Partial<Props> = {
    enableAnchorHeadings: false,
    skipHtml: false,
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      contentRef: null,
    };
  }

  shouldComponentUpdate(nextProps: Readonly<Props>, nextState: Readonly<State>): boolean {
    // We have to check if the contentRef changed and update afterwards so the page can scroll to the anchor links.
    // Otherwise, it can happen that componentDidUpdate is never executed depending on how fast the markdown got rendered
    // We also have to check if props have changed, because we also want to rerender if one of our props has changed
    const propsChanged = Object.entries(nextProps).some(([key, val]) => {
      if (key === "match") {
        return JSON.stringify(this.props[key]) !== JSON.stringify(nextProps[key]);
      }
      return this.props[key as keyof Props] !== val;
    });
    return this.state.contentRef !== nextState.contentRef || propsChanged;
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
      className,
      mdastPlugins = [],
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

    remarkRendererList.image = createMarkdownImageRenderer(basePath);

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
            code: ["className"], // Allow className for code elements, this is necessary to extract the code language
          },
          clobberPrefix: "", // Do not prefix user-provided ids and class names,
          protocols: {
            href: Object.keys(protocolLinkRendererExtensions),
          },
        })
      )
      .use(rehype2react, {
        createElement: React.createElement,
        passNode: true,
        components: createComponentList(remarkRendererList, { permalink }),
      });

    const renderedMarkdown: any = processor.processSync(content).result;

    return (
      <ErrorBoundary fallback={MarkdownErrorNotification}>
        <HorizontalScrollDiv
          ref={(el) => this.setState({ contentRef: el })}
          className={classNames("content", className)}
        >
          {renderedMarkdown}
        </HorizontalScrollDiv>
      </ErrorBoundary>
    );
  }
}

export default withTranslation("repos")(withRouter(LazyMarkdownView));
