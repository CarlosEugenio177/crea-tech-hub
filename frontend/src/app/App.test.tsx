import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { expect, test } from 'vitest'
import { App } from './App.tsx'

function renderAt(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>,
  )
}

test('shows the home page at the root path', () => {
  renderAt('/')

  expect(
    screen.getByRole('heading', { name: 'Seu acesso ao Hub começa aqui' }),
  ).toBeInTheDocument()
})

test('shows the not found page with a way back for unknown paths', () => {
  renderAt('/endereco-inexistente')

  expect(
    screen.getByRole('heading', { name: 'Este espaço não está disponível.' }),
  ).toBeInTheDocument()
  expect(
    screen.getByRole('link', { name: 'Voltar ao início' }),
  ).toHaveAttribute('href', '/')
})
