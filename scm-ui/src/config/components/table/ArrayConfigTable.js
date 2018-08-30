//@flow
import React from "react";
import { RemoveEntryOfTableButton } from "../../../components/buttons";

type Props = {
  items: string[],
  label: string,
  removeLabel: string,
  onRemove: (string[], string) => void,
  disabled: boolean
};

class ArrayConfigTable extends React.Component<Props> {
  render() {
    const { label, disabled, removeLabel, items } = this.props;
    return (
      <div>
        <label className="label">{label}</label>
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {items.map(item => {
              return (
                <tr key={item}>
                  <td>{item}</td>
                  <td>
                    <RemoveEntryOfTableButton
                      entryname={item}
                      removeEntry={this.removeEntry}
                      disabled={disabled}
                      label={removeLabel}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

  removeEntry = (item: string) => {
    const newItems = this.props.items.filter(name => name !== item);
    this.props.onRemove(newItems, item);
  };
}

export default ArrayConfigTable;
