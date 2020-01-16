import React from "react";
import { withTranslation, WithTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
// @ts-ignore
import { Diff as DiffComponent, getChangeKey, Hunk, Decoration } from "react-diff-view";
import { Button, ButtonGroup } from "../buttons";
import Tag from "../Tag";
import Icon from "../Icon";
import { ChangeEvent, Change, File, Hunk as HunkType, DiffObjectProps } from "./DiffTypes";

const EMPTY_ANNOTATION_FACTORY = {};

type Props = DiffObjectProps &
  WithTranslation & {
    file: File;
    defaultCollapse?: boolean;
  };

type Collapsible = {
  collapsed?: boolean;
};

type State = Collapsible & {
  sideBySide?: boolean;
};

const DiffFilePanel = styled.div`
  /* remove bottom border for collapsed panels */
  ${(props: Collapsible) => (props.collapsed ? "border-bottom: none;" : "")};
`;

const FlexWrapLevel = styled.div`
  /* breaks into a second row
     when buttons and title become too long */
  flex-wrap: wrap;
`;

const FullWidthTitleHeader = styled.div`
  max-width: 100%;
`;

const TitleWrapper = styled.span`
  margin-left: 0.25rem;
`;

const ButtonWrapper = styled.div`
  /* align child to right */
  margin-left: auto;
`;

const HunkDivider = styled.hr`
  margin: 0.5rem 0;
`;

const ChangeTypeTag = styled(Tag)`
  margin-left: 0.75rem;
`;

const ModifiedDiffComponent = styled(DiffComponent)`
  /* align line numbers */
  & .diff-gutter {
    text-align: right;
  }
  /* column sizing */
  > colgroup .diff-gutter-col {
    width: 3.25rem;
  }
  /* prevent following content from moving down */
  > .diff-gutter:empty:hover::after {
    font-size: 0.7rem;
  }
  /* smaller font size for code */
  & .diff-line {
    font-size: 0.75rem;
  }
  /* comment padding for sidebyside view */
  &.split .diff-widget-content .is-indented-line {
    padding-left: 3.25rem;
  }
  /* comment padding for combined view */
  &.unified .diff-widget-content .is-indented-line {
    padding-left: 6.5rem;
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
      collapsed: !!this.props.defaultCollapse,
      sideBySide: props.sideBySide
    };
  }

  // collapse diff by clicking collapseDiffs button
  componentDidUpdate(prevProps: Props) {
    const { defaultCollapse } = this.props;
    if (prevProps.defaultCollapse !== defaultCollapse) {
      this.setState({
        collapsed: defaultCollapse
      });
    }
  }

  toggleCollapse = () => {
    const { file } = this.props;
    if (this.hasContent(file)) {
      this.setState(state => ({
        collapsed: !state.collapsed
      }));
    }
  };

  toggleSideBySide = () => {
    this.setState(state => ({
      sideBySide: !state.sideBySide
    }));
  };

  setCollapse = (collapsed: boolean) => {
    this.setState({
      collapsed
    });
  };

  createHunkHeader = (hunk: HunkType, i: number) => {
    if (i > 0) {
      return <HunkDivider />;
    }
    // hunk header must be defined
    return <span />;
  };

  collectHunkAnnotations = (hunk: HunkType) => {
    const { annotationFactory, file } = this.props;
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
    const { file, onClick } = this.props;
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

  renderHunk = (hunk: HunkType, i: number) => {
    if (this.props.markConflicts && hunk.changes) {
      this.markConflicts(hunk);
    }
    return [
      <Decoration key={"decoration-" + hunk.content}>{this.createHunkHeader(hunk, i)}</Decoration>,
      <Hunk
        key={"hunk-" + hunk.content}
        hunk={hunk}
        widgets={this.collectHunkAnnotations(hunk)}
        gutterEvents={this.createGutterEvents(hunk)}
      />
    ];
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

  renderFileTitle = (file: File) => {
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

  hoverFileTitle = (file: File): string => {
    if (file.oldPath !== file.newPath && (file.type === "copy" || file.type === "rename")) {
      return `${file.oldPath} > ${file.newPath}`;
    } else if (file.type === "delete") {
      return file.oldPath;
    }
    return file.newPath;
  };

  renderChangeTag = (file: File) => {
    const { t } = this.props;
    if (!file.type) {
      return;
    }
    const key = "diff.changes." + file.type;
    let value = t(key);
    if (key === value) {
      value = file.type;
    }
    const color =
      value === "added" ? "success is-outlined" : value === "deleted" ? "danger is-outlined" : "info is-outlined";

    return <ChangeTypeTag className={classNames("is-rounded", "has-text-weight-normal")} color={color} label={value} />;
  };

  concat = (array: object[][]) => {
    if (array.length > 0) {
      return array.reduce((a, b) => a.concat(b));
    } else {
      return [];
    }
  };

  hasContent = (file: File) => file && !file.isBinary && file.hunks && file.hunks.length > 0;

  render() {
    const { file, fileControlFactory, fileAnnotationFactory, t } = this.props;
    const { collapsed, sideBySide } = this.state;
    const viewType = sideBySide ? "split" : "unified";

    let body = null;
    let icon = "angle-right";
    if (!collapsed) {
      const fileAnnotations = fileAnnotationFactory ? fileAnnotationFactory(file) : null;
      icon = "angle-down";
      body = (
        <div className="panel-block is-paddingless">
          {fileAnnotations}
          <ModifiedDiffComponent className={viewType} viewType={viewType} hunks={file.hunks} diffType={file.type}>
            {(hunks: HunkType[]) => this.concat(hunks.map(this.renderHunk))}
          </ModifiedDiffComponent>
        </div>
      );
    }
    const collapseIcon = this.hasContent(file) ? <Icon name={icon} color="inherit" /> : null;
    const fileControls = fileControlFactory ? fileControlFactory(file, this.setCollapse) : null;
    const sideBySideToggle =
      file.hunks && file.hunks.length > 0 ? (
        <ButtonWrapper className={classNames("level-right", "is-flex")}>
          <ButtonGroup>
            <Button
              action={this.toggleSideBySide}
              icon={sideBySide ? "align-left" : "columns"}
              label={t(sideBySide ? "diff.combined" : "diff.sideBySide")}
              reducedMobile={true}
            />
            {fileControls}
          </ButtonGroup>
        </ButtonWrapper>
      ) : null;

    return (
      <DiffFilePanel className={classNames("panel", "is-size-6")} collapsed={(file && file.isBinary) || collapsed}>
        <div className="panel-heading">
          <FlexWrapLevel className="level">
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
            {sideBySideToggle}
          </FlexWrapLevel>
        </div>
        {body}
      </DiffFilePanel>
    );
  }
}

export default withTranslation("repos")(DiffFile);
