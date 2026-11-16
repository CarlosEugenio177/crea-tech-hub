import { screen, within } from '@testing-library/react'
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

const ROOM_ID = '5f0c2a51-6f1e-4a52-8d57-0c6f6f2a9b01'
const SESSION = 'GET /api/v1/admin/auth/me'
const LIST = 'GET /api/v1/admin/spaces'
const CREATE = 'POST /api/v1/admin/spaces'
const FIND = `GET /api/v1/admin/spaces/${ROOM_ID}`
const UPDATE = `PUT /api/v1/admin/spaces/${ROOM_ID}`
const STATUS = `PATCH /api/v1/admin/spaces/${ROOM_ID}/status`

const room = {
  id: ROOM_ID,
  name: 'Sala de reunião',
  type: 'ROOM',
  capacity: 8,
  description: 'Sala com projetor.',
  active: true,
  createdAt: '2026-11-15T12:00:00Z',
}
const station = {
  id: '9b1d4c0e-2a3b-4c5d-8e6f-7a8b9c0d1e02',
  name: 'Estação 01',
  type: 'WORKSTATION',
  capacity: 1,
  description: null,
  active: false,
  createdAt: '2026-11-15T12:05:00Z',
}

const account = (permissions: string[]) => () =>
  json({
    id: '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11',
    fullName: 'Ana Gestora',
    email: 'ana@example.com',
    role: 'ADMIN',
    permissions,
    passwordChangeRequired: false,
  })
const page = (items: unknown[], totalPages = 1) =>
  json({ items, page: 0, size: 20, totalElements: items.length, totalPages })
const spacesManager = account(['MANAGE_SPACES'])

afterEach(() => vi.unstubAllGlobals())

test('lists the spaces with type, capacity and status', async () => {
  fakeApi({ [SESSION]: spacesManager, [LIST]: () => page([room, station]) })

  renderApp('/admin/espacos')

  const [first, second] = await screen.findAllByRole('listitem')
  expect(within(first).getByRole('heading')).toHaveTextContent(
    'Sala de reunião',
  )
  expect(first).toHaveTextContent('Sala · 8 pessoas')
  expect(first).toHaveTextContent('Ativo')
  expect(second).toHaveTextContent('Estação individual · 1 pessoa')
  expect(second).toHaveTextContent('Inativo')
})

test('offers the spaces menu only to accounts that can manage spaces', async () => {
  fakeApi({ [SESSION]: account(['MANAGE_ACCESS']) })

  renderApp('/admin')

  await screen.findByRole('link', { name: 'Visão geral' })
  expect(
    screen.queryByRole('link', { name: 'Espaços' }),
  ).not.toBeInTheDocument()
})

test('shows the backend message when the account cannot manage spaces', async () => {
  fakeApi({
    [SESSION]: account(['MANAGE_ACCESS']),
    [LIST]: () => problem(403, 'Acesso negado.'),
  })

  renderApp('/admin/espacos')

  expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado.')
})

test('filters the list by status from the first page', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [SESSION]: spacesManager,
    [LIST]: () => page([room], 2),
  })
  renderApp('/admin/espacos')

  await user.click(await screen.findByRole('button', { name: 'Próxima' }))
  await screen.findByText('Página 2 de 2')
  await user.selectOptions(screen.getByLabelText('Situação'), 'Ativos')

  await screen.findByText('Página 1 de 2')
  expect(api.sent(LIST).map((request) => request.search)).toEqual([
    '?page=0&size=20',
    '?page=1&size=20',
    '?page=0&size=20&active=true',
  ])
})

test('registers a space and returns to the list', async () => {
  const user = userEvent.setup()
  const api = fakeApi({ [SESSION]: spacesManager, [LIST]: () => page([]) })
  api.routes[CREATE] = () => {
    api.routes[LIST] = () => page([room])
    return json(room, 201)
  }
  renderApp('/admin/espacos')

  await user.click(await screen.findByRole('link', { name: 'Novo espaço' }))
  await user.type(screen.getByLabelText('Nome'), '  Sala de reunião ')
  await user.selectOptions(screen.getByLabelText('Tipo'), 'Sala')
  await user.type(screen.getByLabelText('Capacidade'), '8')
  await user.click(screen.getByRole('button', { name: 'Cadastrar espaço' }))

  expect(
    await screen.findByRole('heading', { name: 'Sala de reunião' }),
  ).toBeInTheDocument()
  const [created] = api.sent(CREATE)
  expect(created.headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(JSON.parse(created.body)).toEqual({
    name: 'Sala de reunião',
    type: 'ROOM',
    capacity: 8,
    description: null,
  })
})

test('rejects a workstation for more than one person without calling the API', async () => {
  const user = userEvent.setup()
  const api = fakeApi({ [SESSION]: spacesManager })
  renderApp('/admin/espacos/novo')

  await user.type(await screen.findByLabelText('Nome'), 'Estação 02')
  await user.selectOptions(screen.getByLabelText('Tipo'), 'Estação individual')
  await user.type(screen.getByLabelText('Capacidade'), '2')
  await user.click(screen.getByRole('button', { name: 'Cadastrar espaço' }))

  expect(
    await screen.findByText(
      'Uma estação individual deve ter capacidade para uma pessoa.',
    ),
  ).toBeInTheDocument()
  expect(api.sent(CREATE)).toHaveLength(0)
})

test('edits a space starting from its current data', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [SESSION]: spacesManager,
    [LIST]: () => page([room]),
    [FIND]: () => json(room),
    [UPDATE]: () => json(room),
  })
  renderApp(`/admin/espacos/${ROOM_ID}`)

  const capacity = await screen.findByLabelText('Capacidade')
  expect(screen.getByLabelText('Nome')).toHaveValue('Sala de reunião')
  expect(screen.getByLabelText('Tipo')).toHaveValue('ROOM')
  await user.clear(capacity)
  await user.type(capacity, '10')
  await user.clear(screen.getByLabelText('Descrição (opcional)'))
  await user.click(screen.getByRole('button', { name: 'Salvar alterações' }))

  expect(
    await screen.findByRole('heading', { name: 'Espaços' }),
  ).toBeInTheDocument()
  expect(JSON.parse(api.sent(UPDATE)[0].body)).toEqual({
    name: 'Sala de reunião',
    type: 'ROOM',
    capacity: 10,
    description: null,
  })
})

test('explains when the space to edit does not exist', async () => {
  fakeApi({
    [SESSION]: spacesManager,
    [FIND]: () => problem(404, 'Espaço não encontrado.'),
  })

  renderApp(`/admin/espacos/${ROOM_ID}`)

  expect(await screen.findByRole('alert')).toHaveTextContent(
    'Espaço não encontrado.',
  )
  expect(
    screen.queryByRole('button', { name: 'Salvar alterações' }),
  ).not.toBeInTheDocument()
})

test('deactivates a space and refreshes the list', async () => {
  const user = userEvent.setup()
  const api = fakeApi({ [SESSION]: spacesManager, [LIST]: () => page([room]) })
  api.routes[STATUS] = () => {
    const inactive = { ...room, active: false }
    api.routes[LIST] = () => page([inactive])
    return json(inactive)
  }
  renderApp('/admin/espacos')

  await user.click(await screen.findByRole('button', { name: 'Desativar' }))

  expect(
    await screen.findByRole('button', { name: 'Ativar' }),
  ).toBeInTheDocument()
  expect(screen.getByRole('listitem')).toHaveTextContent('Inativo')
  const [change] = api.sent(STATUS)
  expect(change.headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(JSON.parse(change.body)).toEqual({ active: false })
})
