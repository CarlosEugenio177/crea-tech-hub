import { screen } from '@testing-library/react'
import { expect, test } from 'vitest'
import { renderApp } from '../test/renderApp.tsx'

test('shows the home page at the root path', () => {
  renderApp('/')

  expect(
    screen.getByRole('heading', { name: 'Seu acesso ao Hub começa aqui' }),
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
