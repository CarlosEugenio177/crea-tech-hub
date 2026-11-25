import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, expect, test, vi } from 'vitest'
import {
  CSRF_HEADER,
  CSRF_TOKEN,
  fakeApi,
  json,
  problem,
} from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'

const SESSION = 'GET /api/v1/admin/auth/me'
const SPACES = 'GET /api/v1/admin/spaces'
const LIST = 'GET /api/v1/admin/space-blocks'
const CREATE = 'POST /api/v1/admin/space-blocks'
const BLOCK_ID = '9d4c2c1a-4b11-4c22-8d33-000000000001'
const CANCEL = `POST /api/v1/admin/space-blocks/${BLOCK_ID}/cancel`
const SPACE_ID = '3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca02'

const manager = () =>
  json({
    id: '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11',
    fullName: 'Ana Gestora',
    email: 'ana@example.com',
    role: 'ADMIN',
    permissions: ['MANAGE_SPACES'],
    passwordChangeRequired: false,
  })

const spacesPage = () =>
  json({
    items: [
      {
        id: SPACE_ID,
        name: 'Sala de podcast',
        type: 'ROOM',
        bookingMode: 'WHOLE_SPACE',
        capacity: 4,
        active: true,
        version: 1,
        createdAt: '2026-11-10T10:00:00Z',
      },
    ],
    page: 0,
    size: 100,
    totalElements: 1,
    totalPages: 1,
  })

const activeBlock = {
  id: BLOCK_ID,
  spaceId: SPACE_ID,
  startsAt: '2027-03-10T09:15',
  endsAt: '2027-03-10T12:30',
  reason: 'Manutenção',
  active: true,
  createdBy: '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11',
  createdAt: '2026-11-20T12:00:00Z',
}

afterEach(() => vi.unstubAllGlobals())

test('lists blocks and cancels an active block with CSRF', async () => {
  const user = userEvent.setup()
  let cancelled = false
  const api = fakeApi({
    [SESSION]: manager,
    [SPACES]: spacesPage,
    [LIST]: () =>
      json({
        items: cancelled ? [] : [activeBlock],
        page: 0,
        size: 20,
        totalElements: cancelled ? 0 : 1,
        totalPages: cancelled ? 0 : 1,
      }),
    [CANCEL]: () => {
      cancelled = true
      return json({
        ...activeBlock,
        active: false,
        cancelledBy: '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11',
        cancelledAt: '2026-11-24T15:00:00Z',
      })
    },
  })

  renderApp('/admin/bloqueios')

  expect(
    await screen.findByRole('heading', { name: 'Sala de podcast' }),
  ).toBeInTheDocument()
  expect(screen.getByText('10/03/2027, das 09:15 às 12:30')).toBeInTheDocument()
  expect(screen.getByText('Manutenção')).toBeInTheDocument()

  await user.click(screen.getByRole('button', { name: 'Cancelar bloqueio' }))

  expect(await screen.findByText('Nenhum bloqueio ativo.')).toBeInTheDocument()
  const [cancelReq] = api.sent(CANCEL)
  expect(cancelReq.headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
})

test('shows the reservation conflict and lets the operator adjust the block', async () => {
  const user = userEvent.setup()
  const message =
    'Já existe uma reserva pendente ou confirmada nesse horário. Confira a fila de reservas ou ajuste o intervalo.'
  let attempts = 0
  const api = fakeApi({
    [SESSION]: manager,
    [SPACES]: spacesPage,
    [LIST]: () =>
      json({
        items: [activeBlock],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      }),
    [CREATE]: () =>
      ++attempts === 1 ? problem(409, message) : json(activeBlock, 201),
  })

  renderApp('/admin/bloqueios/novo')

  await user.selectOptions(await screen.findByLabelText('Espaço'), SPACE_ID)
  await user.type(screen.getByLabelText('Data de início'), '10032027')
  await user.type(screen.getByLabelText('Hora de início'), '0915')
  await user.type(screen.getByLabelText('Data de fim'), '10032027')
  await user.type(screen.getByLabelText('Hora de fim'), '1230')
  await user.type(screen.getByLabelText('Motivo'), '  Manutenção  ')
  await user.click(screen.getByRole('button', { name: 'Bloquear horário' }))

  expect(await screen.findByRole('alert')).toHaveTextContent(message)
  expect(screen.getByLabelText('Motivo')).toHaveValue('  Manutenção  ')
  expect(screen.getByLabelText('Data de início')).toHaveValue('10/03/2027')
  const start = screen.getByLabelText('Hora de início')
  await user.clear(start)
  await user.type(start, '1015')
  await user.click(screen.getByRole('button', { name: 'Bloquear horário' }))

  expect(
    await screen.findByRole('heading', { name: 'Bloqueios de horário' }),
  ).toBeInTheDocument()
  expect(api.sent(CREATE)).toHaveLength(2)
  const [createReq, adjustedReq] = api.sent(CREATE)
  expect(JSON.parse(adjustedReq.body).startsAt).toBe('2027-03-10T10:15')
  expect(createReq.headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(JSON.parse(createReq.body)).toEqual({
    spaceId: SPACE_ID,
    startsAt: '2027-03-10T09:15',
    endsAt: '2027-03-10T12:30',
    reason: 'Manutenção',
  })
})

test('rejects an end timestamp that is not after start', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [SESSION]: manager,
    [SPACES]: spacesPage,
  })

  renderApp('/admin/bloqueios/novo')

  await user.selectOptions(await screen.findByLabelText('Espaço'), SPACE_ID)
  await user.type(screen.getByLabelText('Data de início'), '10/03/2027')
  await user.type(screen.getByLabelText('Hora de início'), '12:30')
  await user.type(screen.getByLabelText('Data de fim'), '10/03/2027')
  await user.type(screen.getByLabelText('Hora de fim'), '12:30')
  await user.type(screen.getByLabelText('Motivo'), 'Reunião da diretoria')
  await user.click(screen.getByRole('button', { name: 'Bloquear horário' }))

  expect(
    await screen.findByText('O fim deve ser depois do início.'),
  ).toBeInTheDocument()
  expect(api.sent(CREATE)).toHaveLength(0)
})

test('offers space blocks only to accounts that can manage spaces', async () => {
  fakeApi({
    [SESSION]: () =>
      json({
        id: '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11',
        fullName: 'Ana Gestora',
        email: 'ana@example.com',
        role: 'ADMIN',
        permissions: ['MANAGE_ACCESS'],
        passwordChangeRequired: false,
      }),
  })

  renderApp('/admin')

  await screen.findByRole('heading', { name: 'Visão geral' })
  expect(
    screen.queryByRole('link', { name: 'Bloqueios' }),
  ).not.toBeInTheDocument()
})

test('includes the whole last day in the block filter', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [SESSION]: manager,
    [SPACES]: spacesPage,
    [LIST]: () =>
      json({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }),
  })

  renderApp('/admin/bloqueios')
  await user.type(await screen.findByLabelText('Até'), '31012027')
  await screen.findByText('Nenhum bloqueio encontrado.')
  expect(new URLSearchParams(api.sent(LIST).at(-1)?.search).get('to')).toBe(
    '2027-02-01T00:00',
  )
})
