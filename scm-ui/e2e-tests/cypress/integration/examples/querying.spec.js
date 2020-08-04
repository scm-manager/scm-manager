/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
/// <reference types="cypress" />

context('Querying', () => {
  beforeEach(() => {
    cy.visit('https://example.cypress.io/commands/querying')
  })

  // The most commonly used query is 'cy.get()', you can
  // think of this like the '$' in jQuery

  it('cy.get() - query DOM elements', () => {
    // https://on.cypress.io/get

    cy.get('#query-btn').should('contain', 'Button')

    cy.get('.query-btn').should('contain', 'Button')

    cy.get('#querying .well>button:first').should('contain', 'Button')
    //              ↲
    // Use CSS selectors just like jQuery

    cy.get('[data-test-id="test-example"]').should('have.class', 'example')

    // 'cy.get()' yields jQuery object, you can get its attribute
    // by invoking `.attr()` method
    cy.get('[data-test-id="test-example"]')
      .invoke('attr', 'data-test-id')
      .should('equal', 'test-example')

    // or you can get element's CSS property
    cy.get('[data-test-id="test-example"]')
      .invoke('css', 'position')
      .should('equal', 'static')

    // or use assertions directly during 'cy.get()'
    // https://on.cypress.io/assertions
    cy.get('[data-test-id="test-example"]')
      .should('have.attr', 'data-test-id', 'test-example')
      .and('have.css', 'position', 'static')
  })

  it('cy.contains() - query DOM elements with matching content', () => {
    // https://on.cypress.io/contains
    cy.get('.query-list')
      .contains('bananas')
      .should('have.class', 'third')

    // we can pass a regexp to `.contains()`
    cy.get('.query-list')
      .contains(/^b\w+/)
      .should('have.class', 'third')

    cy.get('.query-list')
      .contains('apples')
      .should('have.class', 'first')

    // passing a selector to contains will
    // yield the selector containing the text
    cy.get('#querying')
      .contains('ul', 'oranges')
      .should('have.class', 'query-list')

    cy.get('.query-button')
      .contains('Save Form')
      .should('have.class', 'btn')
  })

  it('.within() - query DOM elements within a specific element', () => {
    // https://on.cypress.io/within
    cy.get('.query-form').within(() => {
      cy.get('input:first').should('have.attr', 'placeholder', 'Email')
      cy.get('input:last').should('have.attr', 'placeholder', 'Password')
    })
  })

  it('cy.root() - query the root DOM element', () => {
    // https://on.cypress.io/root

    // By default, root is the document
    cy.root().should('match', 'html')

    cy.get('.query-ul').within(() => {
      // In this within, the root is now the ul DOM element
      cy.root().should('have.class', 'query-ul')
    })
  })

  it('best practices - selecting elements', () => {
    // https://on.cypress.io/best-practices#Selecting-Elements
    cy.get('[data-cy=best-practices-selecting-elements]').within(() => {
      // Worst - too generic, no context
      cy.get('button').click()

      // Bad. Coupled to styling. Highly subject to change.
      cy.get('.btn.btn-large').click()

      // Average. Coupled to the `name` attribute which has HTML semantics.
      cy.get('[name=submission]').click()

      // Better. But still coupled to styling or JS event listeners.
      cy.get('#main').click()

      // Slightly better. Uses an ID but also ensures the element
      // has an ARIA role attribute
      cy.get('#main[role=button]').click()

      // Much better. But still coupled to text content that may change.
      cy.contains('Submit').click()

      // Best. Insulated from all changes.
      cy.get('[data-cy=submit]').click()
    })
  })
})
