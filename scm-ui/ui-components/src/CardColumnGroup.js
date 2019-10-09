//@flow
import * as React from "react";
import classNames from "classnames";
import styled from "styled-components";

type Props = {
  name: string,
  elements: React.Node[]
};

type State = {
  collapsed: boolean
};

const Container = styled.div`
  margin-bottom: 1em;
`;

const Wrapper = styled.div`
  padding: 0 0.75rem;
`;

export default class CardColumnGroup extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  toggleCollapse = () => {
    this.setState(prevState => ({
      collapsed: !prevState.collapsed
    }));
  };

  isLastEntry = (array: React.Node[], index: number) => {
    return index === array.length - 1;
  };

  isLengthOdd = (array: React.Node[]) => {
    return array.length % 2 !== 0;
  };

  isFullSize = (array: React.Node[], index: number) => {
    return this.isLastEntry(array, index) && this.isLengthOdd(array);
  };

  render() {
    const { name, elements } = this.props;
    const { collapsed } = this.state;

    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";
    let content = null;
    if (!collapsed) {
      content = elements.map((entry, index) => {
        const fullColumnWidth = this.isFullSize(elements, index);
        const sizeClass = fullColumnWidth ? "is-full" : "is-half";
        return (
          <div
            className={classNames(
              "box",
              "box-link-shadow",
              "column",
              "is-clipped",
              sizeClass
            )}
            key={index}
          >
            {entry}
          </div>
        );
      });
    }
    return (
      <Container className={classNames("is-clearfix")}>
        <h2>
          <span
            className={classNames("is-size-4", "has-cursor-pointer")}
            onClick={this.toggleCollapse}
          >
            <i className={classNames("fa", icon)} /> {name}
          </span>
        </h2>
        <hr />
        <Wrapper className={classNames("columns", "is-multiline")}>
          {content}
        </Wrapper>
      </Container>
    );
  }
}
