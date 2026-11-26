import { screen } from '@testing-library/react'
import { expect, test } from 'vitest'
import { renderApp } from '../test/renderApp.tsx'

test('opens the space catalog at the root path', () => {
  renderApp('/')

  expect(
    screen.getByRole('heading', { name: 'Escolha um espaço' }),
  ).toBeInTheDocument()
})

test('shows the not found page with a way back for unknown paths', () => {
  renderApp('/endereco-inexistente')

  expect(
    screen.getByRole('heading', { name: 'Este espaço não está disponível.' }),
  ).toBeInTheDocument()
  expect(
    screen.getByRole('link', { name: 'Voltar ao início' }),
  ).toHaveAttribute('href', '/')
})
