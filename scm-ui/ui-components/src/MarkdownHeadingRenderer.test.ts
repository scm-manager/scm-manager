import React from 'react';
import { headingToAnchorId } from './MarkdownHeadingRenderer';

describe('headingToAnchorId tests', () => {
  it('should lower case the text', () => {
    expect(headingToAnchorId('Hello')).toBe('hello');
    expect(headingToAnchorId('HeLlO')).toBe('hello');
    expect(headingToAnchorId('HELLO')).toBe('hello');
  });

  it('should replace spaces with hyphen', () => {
    expect(headingToAnchorId('awesome stuff')).toBe('awesome-stuff');
    expect(headingToAnchorId('a b c d e f')).toBe('a-b-c-d-e-f');
  });
});
