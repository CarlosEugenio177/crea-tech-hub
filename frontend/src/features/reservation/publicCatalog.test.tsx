import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, test } from 'vitest'
import { fakeApi, json, problem } from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'
import { CATALOG } from './reservationFixtures.ts'

test('shows an error when the public catalog cannot be loaded', async () => {
  fakeApi({
    [CATALOG]: () => problem(503, 'Catálogo indisponível.'),
  })

  renderApp('/reservar')

  expect(await screen.findByRole('alert')).toHaveTextContent(
    'Catálogo indisponível.',
  )
})

test('shows an empty state when there are no active spaces', async () => {
  fakeApi({
    [CATALOG]: () =>
      json({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }),
  })

  renderApp('/reservar')

  expect(
    await screen.findByText('Nenhum espaço está disponível no momento.'),
  ).toBeInTheDocument()
})

test('reports an empty proxy error without breaking the catalog', async () => {
  fakeApi({ [CATALOG]: () => new Response(null, { status: 503 }) })
  renderApp('/reservar')
  expect(await screen.findByRole('alert')).toHaveTextContent(
    'Não foi possível concluir a solicitação. Tente novamente.',
  )
})

test('loads the next catalog page without losing the spaces already shown', async () => {
  let page = 0
  const api = fakeApi({
    [CATALOG]: () => {
      const current = page++
      return json({
        items: [
          {
            id: `space-${current}`,
            name: current === 0 ? 'Sala inicial' : 'Sala seguinte',
            type: 'ROOM',
            capacity: 4,
            description: null,
          },
        ],
        page: current,
        size: 1,
        totalElements: 2,
        totalPages: 2,
      })
    },
  })
  const user = userEvent.setup()
  renderApp('/reservar')
  expect(
    await screen.findByRole('button', { name: 'Ver horários de Sala inicial' }),
  ).toBeInTheDocument()
  await user.click(
    screen.getByRole('button', { name: 'Carregar mais espaços' }),
  )
  expect(
    await screen.findByRole('button', {
      name: 'Ver horários de Sala seguinte',
    }),
  ).toBeInTheDocument()
  expect(
    screen.getByRole('button', { name: 'Ver horários de Sala inicial' }),
  ).toBeInTheDocument()
  expect(
    screen.queryByRole('button', { name: 'Carregar mais espaços' }),
  ).not.toBeInTheDocument()
  expect(new URLSearchParams(api.sent(CATALOG)[1]?.search).get('page')).toBe(
    '1',
  )
})
