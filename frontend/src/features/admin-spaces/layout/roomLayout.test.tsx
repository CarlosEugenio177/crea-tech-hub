import { act, fireEvent, screen, waitFor } from '@testing-library/react'
import { focusManager } from '@tanstack/react-query'
import userEvent from '@testing-library/user-event'
import { afterEach, expect, test, vi } from 'vitest'
import {
  fakeApi,
  json,
  problem,
  CSRF_HEADER,
  CSRF_TOKEN,
} from '../../../test/fakeApi.ts'
import { renderApp } from '../../../test/renderApp.tsx'
import type { Space } from './layoutTypes.ts'

const ID = '5f0c2a51-6f1e-4a52-8d57-0c6f6f2a9b01'
const ROOM = `GET /api/v1/admin/spaces/${ID}`
const STATIONS = `GET /api/v1/admin/spaces/${ID}/stations`
const MODE = `PUT /api/v1/admin/spaces/${ID}/booking-mode`
const BATCH = `POST /api/v1/admin/spaces/${ID}/stations/batch`
const SAVE = `PUT /api/v1/admin/spaces/${ID}/station-positions`
const room: Space = {
  id: ID,
  name: 'Coworking',
  type: 'ROOM',
  capacity: 16,
  description: null,
  active: true,
  createdAt: '2026-11-17T12:00:00Z',
  bookingMode: 'PER_STATION',
  version: 0,
}
const station = (number: number): Space => ({
  ...room,
  id: `00000000-0000-4000-8000-${String(number).padStart(12, '0')}`,
  name: `Estação ${number}`,
  type: 'WORKSTATION',
  capacity: 1,
  bookingMode: 'WHOLE_SPACE',
  stationPlacement: { roomId: ID, number, x: 25, y: 50 },
})
const page = (items: Space[]) =>
  json({
    items,
    page: 0,
    size: 100,
    totalElements: items.length,
    totalPages: items.length ? 1 : 0,
  })
const base = (items = [station(1)]) => ({
  'GET /api/v1/admin/auth/me': () =>
    json({
      id: '12345678-1234-4234-8234-123456789012',
      fullName: 'Operador',
      email: 'operator@example.com',
      role: 'ADMIN',
      permissions: ['MANAGE_SPACES'],
      passwordChangeRequired: false,
    }),
  [ROOM]: () => json(room),
  [STATIONS]: () => page(items),
})

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
  focusManager.setFocused(undefined)
})

test('changes the booking mode and generates the requested stations', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    ...base([]),
    [ROOM]: () => json({ ...room, bookingMode: 'WHOLE_SPACE' }),
  })
  api.routes[MODE] = () => json({ ...room, version: 1 })
  api.routes[BATCH] = () => {
    const items = [station(1), station(2), station(3)]
    api.routes[STATIONS] = () => page(items)
    return json(items, 201)
  }
  renderApp(`/admin/espacos/${ID}/mapa`)
  await user.selectOptions(
    await screen.findByLabelText('Como será reservado'),
    'PER_STATION',
  )
  await user.click(screen.getByRole('button', { name: 'Salvar modalidade' }))
  const count = await screen.findByLabelText('Quantidade de estações')
  await user.clear(count)
  await user.type(count, '3')
  await user.clear(screen.getByLabelText('Estações por fileira'))
  await user.type(screen.getByLabelText('Estações por fileira'), '2')
  await user.click(screen.getByRole('button', { name: 'Gerar estações' }))
  expect(
    await screen.findByRole('button', { name: 'Mover estação 3' }),
  ).toBeInTheDocument()
  expect(JSON.parse(api.sent(BATCH)[0].body)).toEqual({ count: 3, columns: 2 })
  expect(api.sent(BATCH)[0].headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
})

test('moves with the keyboard and persists only after saving', async () => {
  const user = userEvent.setup()
  const api = fakeApi(base())
  api.routes[SAVE] = () =>
    json([
      {
        ...station(1),
        version: 1,
        stationPlacement: { roomId: ID, number: 1, x: 27, y: 50 },
      },
    ])
  renderApp(`/admin/espacos/${ID}/mapa`)
  const seat = await screen.findByRole('button', { name: 'Mover estação 1' })
  await user.click(seat)
  await user.keyboard('{ArrowRight}')
  expect(seat.style.left).toBe('27%')
  expect(api.sent(SAVE)).toHaveLength(0)
  await user.click(screen.getByRole('button', { name: 'Salvar mapa' }))
  expect(await screen.findByText('Mapa salvo.')).toBeInTheDocument()
  expect(JSON.parse(api.sent(SAVE)[0].body)).toEqual({
    positions: [{ id: station(1).id, version: 0, x: 27, y: 50 }],
  })
  expect(seat.style.left).toBe('27%')
  expect(screen.getByRole('button', { name: 'Salvar mapa' })).toBeDisabled()
})

test('drags within the room bounds and can discard the movement', async () => {
  vi.stubGlobal('PointerEvent', MouseEvent)
  const user = userEvent.setup()
  const api = fakeApi(base())
  renderApp(`/admin/espacos/${ID}/mapa`)
  const seat = await screen.findByRole('button', { name: 'Mover estação 1' })
  const canvas = screen.getByRole('group', { name: 'Mapa de estações' })
  vi.spyOn(canvas, 'getBoundingClientRect').mockReturnValue(
    new DOMRect(0, 0, 200, 200),
  )
  fireEvent.pointerDown(seat, {
    button: 0,
    clientX: 50,
    clientY: 100,
    pointerId: 1,
  })
  fireEvent.pointerMove(seat, { clientX: 500, clientY: -100, pointerId: 1 })
  fireEvent.pointerUp(seat, { pointerId: 1 })
  expect(seat.style.left).toBe('100%')
  expect(seat.style.top).toBe('0%')
  await user.click(screen.getByRole('button', { name: 'Descartar' }))
  expect(seat.style.left).toBe('25%')
  expect(api.sent(SAVE)).toHaveLength(0)
})

test('preserves a draft on conflict and lets the administrator reload explicitly', async () => {
  const user = userEvent.setup()
  const api = fakeApi(base())
  api.routes[SAVE] = () => problem(409, 'O mapa foi alterado por outra pessoa.')
  renderApp(`/admin/espacos/${ID}/mapa`)
  const seat = await screen.findByRole('button', { name: 'Mover estação 1' })
  await user.click(seat)
  await user.keyboard('{ArrowRight}')
  await user.click(screen.getByRole('button', { name: 'Salvar mapa' }))
  expect(await screen.findByRole('alert')).toHaveTextContent(
    'O mapa foi alterado por outra pessoa.',
  )
  expect(seat.style.left).toBe('27%')
  api.routes[STATIONS] = () =>
    page([
      {
        ...station(1),
        version: 1,
        stationPlacement: { roomId: ID, number: 1, x: 60, y: 50 },
      },
    ])
  await user.click(
    screen.getByRole('button', { name: 'Descartar e recarregar' }),
  )
  await waitFor(() => expect(seat.style.left).toBe('60%'))
})

test('shows a permission error without rendering the editor', async () => {
  fakeApi({ ...base(), [ROOM]: () => problem(403, 'Acesso negado.') })
  renderApp(`/admin/espacos/${ID}/mapa`)
  expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado.')
  expect(
    screen.queryByRole('group', { name: 'Mapa de estações' }),
  ).not.toBeInTheDocument()
})

test('loads every station page before showing the editor', async () => {
  let next = 0
  const api = fakeApi({
    ...base(),
    [STATIONS]: () => {
      const current = next++
      return json({
        items: [station(current + 1)],
        page: current,
        size: 1,
        totalElements: 2,
        totalPages: 2,
      })
    },
  })
  renderApp(`/admin/espacos/${ID}/mapa`)
  expect(
    await screen.findByRole('button', { name: 'Mover estação 2' }),
  ).toBeInTheDocument()
  expect(
    screen.getByRole('button', { name: 'Mover estação 1' }),
  ).toBeInTheDocument()
  expect(
    api
      .sent(STATIONS)
      .map((request) => new URLSearchParams(request.search).get('page')),
  ).toEqual(['0', '1'])
})

test('keeps unsaved positions when a background refresh fails', async () => {
  const user = userEvent.setup()
  const api = fakeApi(base())
  renderApp(`/admin/espacos/${ID}/mapa`)
  const seat = await screen.findByRole('button', { name: 'Mover estação 1' })
  await user.click(seat)
  await user.keyboard('{ArrowRight}')
  api.routes[STATIONS] = () =>
    problem(503, 'Mapa temporariamente indisponível.')
  act(() => focusManager.setFocused(false))
  act(() => focusManager.setFocused(true))
  expect(await screen.findByRole('alert')).toHaveTextContent(
    'Mapa temporariamente indisponível.',
  )
  expect(seat.style.left).toBe('27%')
  expect(
    screen.getByRole('button', { name: 'Adicionar estações' }),
  ).toBeDisabled()
  api.routes[STATIONS] = () => page([station(1)])
  await user.click(screen.getByRole('button', { name: 'Recarregar mapa' }))
  await waitFor(() => expect(seat).toBeEnabled())
  expect(seat.style.left).toBe('27%')
})
