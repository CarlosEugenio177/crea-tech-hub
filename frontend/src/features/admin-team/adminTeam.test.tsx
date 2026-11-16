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

const MANAGER_ID = '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11'
const STAFF_ID = '7c2e1a90-3b4d-4e5f-9a6b-1c2d3e4f5a22'
const SESSION = 'GET /api/v1/admin/auth/me'
const TEAM = 'GET /api/v1/admin/team'
const OPTIONS = 'GET /api/v1/admin/team/permissions'
const AUDIT = 'GET /api/v1/admin/team/audit'
const CREATE = 'POST /api/v1/admin/team'
const MEMBER = `GET /api/v1/admin/team/${STAFF_ID}`
const PERMISSIONS = `PUT /api/v1/admin/team/${STAFF_ID}/permissions`
const STATUS = `PATCH /api/v1/admin/team/${STAFF_ID}/status`

const manager = {
  id: MANAGER_ID,
  fullName: 'Ana Gestora',
  email: 'ana@example.com',
  role: 'ADMIN',
  active: true,
  permissions: ['MANAGE_ACCESS', 'MANAGE_SPACES'],
  passwordChangeRequired: false,
  createdAt: '2026-11-10T12:00:00Z',
}
const staff = {
  id: STAFF_ID,
  fullName: 'Bruno Recepção',
  email: 'bruno@example.com',
  role: 'ADMIN',
  active: true,
  permissions: ['MANAGE_SPACES'],
  passwordChangeRequired: true,
  createdAt: '2026-11-15T12:00:00Z',
}

const page = (items: unknown[]) =>
  json({ items, page: 0, size: 20, totalElements: items.length, totalPages: 1 })
const base = () => ({
  [SESSION]: () => json(manager),
  [TEAM]: () => page([manager, staff]),
  [OPTIONS]: () =>
    json([
      {
        permission: 'MANAGE_ACCESS',
        description: 'Gerenciar equipe e permissões',
      },
      { permission: 'MANAGE_SPACES', description: 'Gerenciar espaços' },
    ]),
})

afterEach(() => vi.unstubAllGlobals())

test('lists the team and keeps the own account without access actions', async () => {
  fakeApi(base())

  renderApp('/admin/equipe')

  const [own, other] = await screen.findAllByRole('listitem')
  expect(own).toHaveTextContent('Ana Gestora')
  expect(within(own).queryByRole('button')).not.toBeInTheDocument()
  expect(other).toHaveTextContent('bruno@example.com')
  expect(other).toHaveTextContent('Aguardando o primeiro acesso.')
  expect(
    await within(other).findByText('Gerenciar espaços'),
  ).toBeInTheDocument()
  expect(
    within(other).getByRole('button', { name: 'Desativar' }),
  ).toBeInTheDocument()
})

test('registers an employee with the chosen permissions', async () => {
  const user = userEvent.setup()
  const api = fakeApi({ ...base(), [CREATE]: () => json(staff, 201) })
  renderApp('/admin/equipe/novo')

  await user.type(
    await screen.findByLabelText('Nome completo'),
    'Bruno Recepção',
  )
  await user.type(screen.getByLabelText('E-mail'), 'bruno@example.com')
  await user.type(screen.getByLabelText('Senha inicial'), 'senha-inicial-123')
  await user.click(screen.getByLabelText('Gerenciar espaços'))
  await user.click(
    screen.getByRole('button', { name: 'Cadastrar funcionário' }),
  )

  expect(
    await screen.findByRole('heading', { name: 'Equipe' }),
  ).toBeInTheDocument()
  const [created] = api.sent(CREATE)
  expect(created.headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(JSON.parse(created.body)).toEqual({
    fullName: 'Bruno Recepção',
    email: 'bruno@example.com',
    initialPassword: 'senha-inicial-123',
    permissions: ['MANAGE_SPACES'],
  })
})

test('rejects a short initial password without calling the API', async () => {
  const user = userEvent.setup()
  const api = fakeApi(base())
  renderApp('/admin/equipe/novo')

  await user.type(await screen.findByLabelText('Nome completo'), 'Bruno')
  await user.type(screen.getByLabelText('E-mail'), 'bruno@example.com')
  await user.type(screen.getByLabelText('Senha inicial'), 'curta')
  await user.click(
    screen.getByRole('button', { name: 'Cadastrar funcionário' }),
  )

  expect(
    await screen.findByText('Use pelo menos 12 caracteres.'),
  ).toBeInTheDocument()
  expect(api.sent(CREATE)).toHaveLength(0)
})

test('replaces the permissions of another account', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    ...base(),
    [MEMBER]: () => json(staff),
    [PERMISSIONS]: () => json(staff),
  })
  renderApp(`/admin/equipe/${STAFF_ID}`)

  const spaces = await screen.findByLabelText('Gerenciar espaços')
  expect(spaces).toBeChecked()
  await user.click(spaces)
  await user.click(screen.getByLabelText('Gerenciar equipe e permissões'))
  await user.click(screen.getByRole('button', { name: 'Salvar permissões' }))

  expect(
    await screen.findByRole('heading', { name: 'Equipe' }),
  ).toBeInTheDocument()
  expect(JSON.parse(api.sent(PERMISSIONS)[0].body)).toEqual({
    permissions: ['MANAGE_ACCESS'],
  })
})

test('does not offer the permissions form for the own account', async () => {
  fakeApi({
    ...base(),
    [`GET /api/v1/admin/team/${MANAGER_ID}`]: () => json(manager),
  })

  renderApp(`/admin/equipe/${MANAGER_ID}`)

  expect(
    await screen.findByText(/Você não pode alterar o próprio acesso/),
  ).toBeInTheDocument()
  expect(
    screen.queryByRole('button', { name: 'Salvar permissões' }),
  ).not.toBeInTheDocument()
})

test('shows the backend reason when a status change is refused', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    ...base(),
    [STATUS]: () => problem(409, 'O sistema precisa de um gestor ativo.'),
  })
  renderApp('/admin/equipe')

  await user.click(await screen.findByRole('button', { name: 'Desativar' }))

  expect(await screen.findByRole('alert')).toHaveTextContent(
    'O sistema precisa de um gestor ativo.',
  )
  expect(JSON.parse(api.sent(STATUS)[0].body)).toEqual({ active: false })
})

test('shows the access history with the names of the accounts', async () => {
  fakeApi({
    ...base(),
    [AUDIT]: () =>
      page([
        {
          id: 'a1b2c3d4-0000-4000-8000-000000000001',
          actorId: MANAGER_ID,
          targetId: STAFF_ID,
          action: 'STATUS_CHANGED',
          detail: 'true -> false',
          createdAt: '2026-11-15T15:30:00Z',
        },
      ]),
  })

  renderApp('/admin/equipe/auditoria')

  const event = await screen.findByRole('listitem')
  expect(within(event).getByRole('heading')).toHaveTextContent(
    'Situação alterada',
  )
  expect(event).toHaveTextContent('Conta desativada.')
  expect(
    await within(event).findByText(/Conta: Bruno Recepção/),
  ).toHaveTextContent('Por: Ana Gestora')
})
