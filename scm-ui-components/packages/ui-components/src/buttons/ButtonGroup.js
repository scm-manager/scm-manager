// @flow
import React from "react";
import Button from "./Button";

type Props = {
  firstlabel: string,
  secondlabel: string,
  firstAction?: (event: Event) => void,
  secondAction?: (event: Event) => void,
  firstIsSelected: boolean
};

class ButtonGroup extends React.Component<Props> {

  render() {
    const { firstlabel, secondlabel, firstAction, secondAction, firstIsSelected } = this.props;

    let showFirstColor = "";
    let showSecondColor = "";

    if (firstIsSelected) {
      showFirstColor += "link is-selected";
    } else {
      showSecondColor += "link is-selected";
    }

    return (
      <div className="buttons has-addons">
        <Button
          label={firstlabel}
          color={showFirstColor}
          action={firstAction}
        />
        <Button
          label={secondlabel}
          color={showSecondColor}
          action={secondAction}
        />
      </div>
    );
  }
}

export default ButtonGroup;
