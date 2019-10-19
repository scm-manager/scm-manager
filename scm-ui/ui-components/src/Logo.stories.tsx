import React from 'react';
import { storiesOf } from '@storybook/react';
import styled from 'styled-components';
import Logo from './Logo';

const Wrapper = styled.div`
  padding: 2em;
  background-color: black;
  height: 100%;
`;

storiesOf('Logo', module).add('Default', () => (
  <Wrapper>
    <Logo />
  </Wrapper>
));
