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
import React from "react";
import { withTranslation, WithTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
// @ts-ignore
import { Decoration, getChangeKey, Hunk } from "react-diff-view";
import { ButtonGroup } from "../buttons";
import Tag from "../Tag";
import Icon from "../Icon";
import { Change, FileDiff, Hunk as HunkType } from "@scm-manager/ui-types";
import { ChangeEvent, DiffObjectProps } from "./DiffTypes";
import TokenizedDiffView from "./TokenizedDiffView";
import DiffButton from "./DiffButton";
import { MenuContext, OpenInFullscreenButton } from "@scm-manager/ui-components";
import DiffExpander, { ExpandableHunk } from "./DiffExpander";
import HunkExpandLink from "./HunkExpandLink";
import { Modal } from "../modals";
import ErrorNotification from "../ErrorNotification";
import HunkExpandDivider from "./HunkExpandDivider";
import { escapeWhitespace } from "./diffs";

const EMPTY_ANNOTATION_FACTORY = {};

type Props = DiffObjectProps &
  WithTranslation & {
    file: FileDiff;
  };

type Collapsible = {
  collapsed?: boolean;
};

type State = Collapsible & {
  file: FileDiff;
  sideBySide?: boolean;
  diffExpander: DiffExpander;
  expansionError?: any;
};

const DiffFilePanel = styled.div`
  /* remove bottom border for collapsed panels */
  ${(props: Collapsible) => (props.collapsed ? "border-bottom: none;" : "")};
`;

const FullWidthTitleHeader = styled.div`
  max-width: 100%;
`;

const TitleWrapper = styled.span`
  margin-left: 0.25rem;
`;

const AlignRight = styled.div`
  margin-left: auto;
`;

const HunkDivider = styled.hr`
  margin: 0.5rem 0;
`;

const ChangeTypeTag = styled(Tag)`
  margin-left: 0.75rem;
`;

const MarginlessModalContent = styled.div`
  margin: -1.25rem;

  & .panel-block {
    flex-direction: column;
    align-items: stretch;
  }
`;

class DiffFile extends React.Component<Props, State> {
  static defaultProps: Partial<Props> = {
    defaultCollapse: false,
    markConflicts: true
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: this.defaultCollapse(),
      sideBySide: props.sideBySide,
      diffExpander: new DiffExpander(props.file),
      file: props.file
    };
  }

  componentDidUpdate(prevProps: Readonly<Props>) {
    if (this.props.defaultCollapse !== prevProps.defaultCollapse) {
      this.setState({
        collapsed: this.defaultCollapse()
      });
    }
  }

  defaultCollapse: () => boolean = () => {
    const { defaultCollapse, file } = this.props;
    if (typeof defaultCollapse === "boolean") {
      return defaultCollapse;
    } else if (typeof defaultCollapse === "function") {
      return defaultCollapse(file.oldPath, file.newPath);
    } else {
      return false;
    }
  };

  toggleCollapse = () => {
    const { file } = this.state;
    if (this.hasContent(file)) {
      this.setState(state => ({
        collapsed: !state.collapsed
      }));
    }
  };

  toggleSideBySide = (callback: () => void) => {
    this.setState(
      state => ({
        sideBySide: !state.sideBySide
      }),
      () => callback()
    );
  };

  setCollapse = (collapsed: boolean) => {
    this.setState({
      collapsed
    });
  };

  createHunkHeader = (expandableHunk: ExpandableHunk) => {
    if (expandableHunk.maxExpandHeadRange > 0) {
      if (expandableHunk.maxExpandHeadRange <= 10) {
        return (
          <HunkExpandDivider>
            <HunkExpandLink
              icon={"fa-angle-double-up"}
              onClick={this.expandHead(expandableHunk, expandableHunk.maxExpandHeadRange)}
              text={this.props.t("diff.expandComplete", { count: expandableHunk.maxExpandHeadRange })}
            />
          </HunkExpandDivider>
        );
      } else {
        return (
          <HunkExpandDivider>
            <HunkExpandLink
              icon={"fa-angle-up"}
              onClick={this.expandHead(expandableHunk, 10)}
              text={this.props.t("diff.expandByLines", { count: 10 })}
            />{" "}
            <HunkExpandLink
              icon={"fa-angle-double-up"}
              onClick={this.expandHead(expandableHunk, expandableHunk.maxExpandHeadRange)}
              text={this.props.t("diff.expandComplete", { count: expandableHunk.maxExpandHeadRange })}
            />
          </HunkExpandDivider>
        );
      }
    }
    // hunk header must be defined
    return <span />;
  };

  createHunkFooter = (expandableHunk: ExpandableHunk) => {
    if (expandableHunk.maxExpandBottomRange > 0) {
      if (expandableHunk.maxExpandBottomRange <= 10) {
        return (
          <HunkExpandDivider>
            <HunkExpandLink
              icon={"fa-angle-double-down"}
              onClick={this.expandBottom(expandableHunk, expandableHunk.maxExpandBottomRange)}
              text={this.props.t("diff.expandComplete", { count: expandableHunk.maxExpandBottomRange })}
            />
          </HunkExpandDivider>
        );
      } else {
        return (
          <HunkExpandDivider>
            <HunkExpandLink
              icon={"fa-angle-down"}
              onClick={this.expandBottom(expandableHunk, 10)}
              text={this.props.t("diff.expandByLines", { count: 10 })}
            />{" "}
            <HunkExpandLink
              icon={"fa-angle-double-down"}
              onClick={this.expandBottom(expandableHunk, expandableHunk.maxExpandBottomRange)}
              text={this.props.t("diff.expandComplete", { count: expandableHunk.maxExpandBottomRange })}
            />
          </HunkExpandDivider>
        );
      }
    }
    // hunk footer must be defined
    return <span />;
  };

  createLastHunkFooter = (expandableHunk: ExpandableHunk) => {
    if (expandableHunk.maxExpandBottomRange !== 0) {
      return (
        <HunkExpandDivider>
          <HunkExpandLink
            icon={"fa-angle-down"}
            onClick={this.expandBottom(expandableHunk, 10)}
            text={this.props.t("diff.expandLastBottomByLines", { count: 10 })}
          />{" "}
          <HunkExpandLink
            icon={"fa-angle-double-down"}
            onClick={this.expandBottom(expandableHunk, expandableHunk.maxExpandBottomRange)}
            text={this.props.t("diff.expandLastBottomComplete")}
          />
        </HunkExpandDivider>
      );
    }
    // hunk header must be defined
    return <span />;
  };

  expandHead = (expandableHunk: ExpandableHunk, count: number) => {
    return () => {
      return expandableHunk
        .expandHead(count)
        .then(this.diffExpanded)
        .catch(this.diffExpansionFailed);
    };
  };

  expandBottom = (expandableHunk: ExpandableHunk, count: number) => {
    return () => {
      return expandableHunk
        .expandBottom(count)
        .then(this.diffExpanded)
        .catch(this.diffExpansionFailed);
    };
  };

  diffExpanded = (newFile: FileDiff) => {
    this.setState({ file: newFile, diffExpander: new DiffExpander(newFile) });
  };

  diffExpansionFailed = (err: any) => {
    this.setState({ expansionError: err });
  };

  collectHunkAnnotations = (hunk: HunkType) => {
    const { annotationFactory } = this.props;
    const { file } = this.state;
    if (annotationFactory) {
      return annotationFactory({
        hunk,
        file
      });
    } else {
      return EMPTY_ANNOTATION_FACTORY;
    }
  };

  handleClickEvent = (change: Change, hunk: HunkType) => {
    const { onClick } = this.props;
    const { file } = this.state;
    const context = {
      changeId: getChangeKey(change),
      change,
      hunk,
      file
    };
    if (onClick) {
      onClick(context);
    }
  };

  createGutterEvents = (hunk: HunkType) => {
    const { onClick } = this.props;
    if (onClick) {
      return {
        onClick: (event: ChangeEvent) => {
          this.handleClickEvent(event.change, hunk);
        }
      };
    }
  };

  renderHunk = (file: FileDiff, expandableHunk: ExpandableHunk, i: number) => {
    const hunk = expandableHunk.hunk;
    if (this.props.markConflicts && hunk.changes) {
      this.markConflicts(hunk);
    }
    const items = [];
    if (file._links?.lines) {
      items.push(this.createHunkHeader(expandableHunk));
    } else if (i > 0) {
      items.push(
        <Decoration>
          <HunkDivider />
        </Decoration>
      );
    }

    items.push(
      <Hunk
        key={"hunk-" + hunk.content}
        hunk={expandableHunk.hunk}
        widgets={this.collectHunkAnnotations(hunk)}
        gutterEvents={this.createGutterEvents(hunk)}
        className={this.props.hunkClass ? this.props.hunkClass(hunk) : null}
      />
    );
    if (file._links?.lines) {
      if (i === file.hunks!.length - 1) {
        items.push(this.createLastHunkFooter(expandableHunk));
      } else {
        items.push(this.createHunkFooter(expandableHunk));
      }
    }
    return items;
  };

  markConflicts = (hunk: HunkType) => {
    let inConflict = false;
    for (let i = 0; i < hunk.changes.length; ++i) {
      if (hunk.changes[i].content === "<<<<<<< HEAD") {
        inConflict = true;
      }
      if (inConflict) {
        hunk.changes[i].type = "conflict";
      }
      if (hunk.changes[i].content.startsWith(">>>>>>>")) {
        inConflict = false;
      }
    }
  };

  getAnchorId(file: FileDiff) {
    let path: string;
    if (file.type === "delete") {
      path = file.oldPath;
    } else {
      path = file.newPath;
    }
    return escapeWhitespace(path);
  }

  renderFileTitle = (file: FileDiff) => {
    if (file.oldPath !== file.newPath && (file.type === "copy" || file.type === "rename")) {
      return (
        <>
          {file.oldPath} <Icon name="arrow-right" color="inherit" /> {file.newPath}
        </>
      );
    } else if (file.type === "delete") {
      return file.oldPath;
    }
    return file.newPath;
  };

  hoverFileTitle = (file: FileDiff): string => {
    if (file.oldPath !== file.newPath && (file.type === "copy" || file.type === "rename")) {
      return `${file.oldPath} > ${file.newPath}`;
    } else if (file.type === "delete") {
      return file.oldPath;
    }
    return file.newPath;
  };

  renderChangeTag = (file: FileDiff) => {
    const { t } = this.props;
    if (!file.type) {
      return;
    }
    const key = "diff.changes." + file.type;
    let value = t(key);
    if (key === value) {
      value = file.type;
    }

    const color = value === "added" ? "success" : value === "deleted" ? "danger" : "info";
    return (
      <ChangeTypeTag
        className={classNames("has-text-weight-normal")}
        rounded={true}
        outlined={true}
        color={color}
        label={value}
      />
    );
  };

  hasContent = (file: FileDiff) => file && !file.isBinary && file.hunks && file.hunks.length > 0;

  render() {
    const { fileControlFactory, fileAnnotationFactory, t } = this.props;
    const { file, collapsed, sideBySide, diffExpander, expansionError } = this.state;
    const viewType = sideBySide ? "split" : "unified";

    const fileAnnotations = fileAnnotationFactory ? fileAnnotationFactory(file) : null;
    const innerContent = (
      <div className="panel-block is-paddingless">
        {fileAnnotations}
        <TokenizedDiffView className={viewType} viewType={viewType} file={file}>
          {(hunks: HunkType[]) =>
            hunks?.map((hunk, n) => {
              return this.renderHunk(file, diffExpander.getHunk(n), n);
            })
          }
        </TokenizedDiffView>
      </div>
    );
    let icon = "angle-right";
    let body = null;
    if (!collapsed) {
      icon = "angle-down";
      body = innerContent;
    }
    const collapseIcon = this.hasContent(file) ? <Icon name={icon} color="inherit" /> : null;
    const fileControls = fileControlFactory ? fileControlFactory(file, this.setCollapse) : null;
    const openInFullscreen = file?.hunks?.length ? (
      <OpenInFullscreenButton
        modalTitle={file.type === "delete" ? file.oldPath : file.newPath}
        modalBody={<MarginlessModalContent>{innerContent}</MarginlessModalContent>}
      />
    ) : null;
    const sideBySideToggle = file?.hunks?.length && (
      <MenuContext.Consumer>
        {({ setCollapsed }) => (
          <DiffButton
            icon={sideBySide ? "align-left" : "columns"}
            tooltip={t(sideBySide ? "diff.combined" : "diff.sideBySide")}
            onClick={() =>
              this.toggleSideBySide(() => {
                if (this.state.sideBySide) {
                  setCollapsed(true);
                }
              })
            }
          />
        )}
      </MenuContext.Consumer>
    );
    const headerButtons = (
      <AlignRight className={classNames("level-right", "is-flex")}>
        <ButtonGroup>
          {sideBySideToggle}
          {openInFullscreen}
          {fileControls}
        </ButtonGroup>
      </AlignRight>
    );

    let errorModal;
    if (expansionError) {
      errorModal = (
        <Modal
          title={t("diff.expansionFailed")}
          closeFunction={() => this.setState({ expansionError: undefined })}
          body={<ErrorNotification error={expansionError} />}
          active={true}
        />
      );
    }

    return (
      <DiffFilePanel
        className={classNames("panel", "is-size-6")}
        collapsed={(file && file.isBinary) || collapsed}
        id={this.getAnchorId(file)}
      >
        {errorModal}
        <div className="panel-heading">
          <div className={classNames("level", "is-flex-wrap-wrap")}>
            <FullWidthTitleHeader
              className={classNames("level-left", "is-flex", "has-cursor-pointer")}
              onClick={this.toggleCollapse}
              title={this.hoverFileTitle(file)}
            >
              {collapseIcon}
              <TitleWrapper className={classNames("is-ellipsis-overflow", "is-size-6")}>
                {this.renderFileTitle(file)}
              </TitleWrapper>
              {this.renderChangeTag(file)}
            </FullWidthTitleHeader>
            {headerButtons}
          </div>
        </div>
        {body}
      </DiffFilePanel>
    );
  }
}

export default withTranslation("repos")(DiffFile);
