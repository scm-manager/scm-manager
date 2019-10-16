// @flow
import * as React from "react";
import classNames from "classnames";
import styled from "styled-components";

const Flex = styled.div`
  &.field:not(:last-child) {
    margin-bottom: 0;
  }
`;

type Props = {
  className?: string,
  children: React.Node
};

class ButtonAddons extends React.Component<Props> {
  render() {
    const { className, children } = this.props;

    const childWrapper = [];
    React.Children.forEach(children, child => {
      if (child) {
        childWrapper.push(
          <p className="control" key={childWrapper.length}>
            {child}
          </p>
        );
      }
    });

    return (
      <Flex className={classNames("field", "has-addons", className)}>
        {childWrapper}
      </Flex>
    );
  }
}

export default ButtonAddons;
