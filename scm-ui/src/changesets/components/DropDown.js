// @flow

import React from "react";

type Props = {
  options: string[],
  optionSelected: string => void
}

class DropDown extends React.Component<Props> {
  render() {
    const {options} = this.props;
    return <select onChange={this.change}>
      {options.map(option => {
        return <option key={option} value={option}>{option}</option>
      })}
    </select>
  }

  change = (event) => {
    this.props.optionSelected(event.target.value);
  }
}

export default DropDown;
