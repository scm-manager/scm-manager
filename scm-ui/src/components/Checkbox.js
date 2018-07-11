//@flow
import React from "react";

type Props = {
  label: string,
  onChange: boolean => void
};
class Checkbox extends React.Component<Props> {
  onCheckboxChange = (event: SyntheticInputEvent<HTMLInputElement>) => {
    this.props.onChange(event.target.checked);
  };

  render() {
    return (
      <div className="field">
        <div className="control">
          <label className="checkbox">
            <input type="checkbox" onChange={this.onCheckboxChange} />{" "}
            {this.props.label}
          </label>
        </div>
      </div>
    );
  }
}

export default Checkbox;
