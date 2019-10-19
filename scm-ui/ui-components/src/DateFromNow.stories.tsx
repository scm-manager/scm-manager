import React from 'react';
import DateFromNow from './DateFromNow';
import { storiesOf } from '@storybook/react';

const baseProps = {
  timeZone: 'Europe/Berlin',
  baseDate: '2019-10-12T13:56:42+02:00',
};

storiesOf('DateFromNow', module).add('Default', () => (
  <div>
    <p>
      <DateFromNow date="2009-06-30T18:30:00+02:00" {...baseProps} />
    </p>
    <p>
      <DateFromNow date="2019-06-30T18:30:00+02:00" {...baseProps} />
    </p>
    <p>
      <DateFromNow date="2019-10-12T13:56:40+02:00" {...baseProps} />
    </p>
    <p>
      <DateFromNow date="2019-10-11T13:56:40+02:00" {...baseProps} />
    </p>
  </div>
));
