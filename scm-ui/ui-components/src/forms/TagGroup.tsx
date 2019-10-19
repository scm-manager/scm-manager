import * as React from 'react';
import { DisplayedUser } from '@scm-manager/ui-types';
import { Help, Tag } from '../index';

type Props = {
  items: DisplayedUser[];
  label: string;
  helpText?: string;
  onRemove: (p: DisplayedUser[]) => void;
};

export default class TagGroup extends React.Component<Props> {
  render() {
    const { items, label, helpText } = this.props;
    let help = null;
    if (helpText) {
      help = <Help className="is-relative" message={helpText} />;
    }
    return (
      <div className="field is-grouped is-grouped-multiline">
        {label && items ? (
          <div className="control">
            <strong>
              {label}
              {help}
              {items.length > 0 ? ':' : ''}
            </strong>
          </div>
        ) : (
          ''
        )}
        {items.map((item, key) => {
          return (
            <div className="control" key={key}>
              <div className="tags has-addons">
                <Tag
                  color="info is-outlined"
                  label={item.displayName}
                  onRemove={() => this.removeEntry(item)}
                />
              </div>
            </div>
          );
        })}
      </div>
    );
  }

  removeEntry = item => {
    const newItems = this.props.items.filter(name => name !== item);
    this.props.onRemove(newItems);
  };
}
