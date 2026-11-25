import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, expect, test, vi } from 'vitest'
import { CSRF_HEADER, CSRF_TOKEN, fakeApi, json } from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'

const SESSION = 'GET /api/v1/admin/auth/me'
const FIND = 'GET /api/v1/admin/hub/operating-hours'
const UPDATE = 'PUT /api/v1/admin/hub/operating-hours'

const manager = () =>
  json({
    id: '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11',
    fullName: 'Ana Gestora',
    email: 'ana@example.com',
    role: 'ADMIN',
    permissions: ['MANAGE_SPACES'],
    passwordChangeRequired: false,
  })

afterEach(() => vi.unstubAllGlobals())

test('loads and updates the Hub operating hours', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [SESSION]: manager,
    [FIND]: () =>
      json({
        daysOfWeek: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
        openingTime: '08:00',
        closingTime: '18:00',
      }),
    [UPDATE]: () =>
      json({
        daysOfWeek: ['MONDAY', 'WEDNESDAY', 'FRIDAY'],
        openingTime: '09:00',
        closingTime: '17:30',
      }),
  })

  renderApp('/admin/funcionamento')

  expect(await screen.findByLabelText('Segunda-feira')).toBeChecked()
  expect(screen.getByLabelText('Abertura')).toHaveValue('08:00')
  expect(screen.getByLabelText('Fechamento')).toHaveValue('18:00')

  await user.click(screen.getByLabelText('Terça-feira'))
  await user.click(screen.getByLabelText('Quinta-feira'))
  await user.clear(screen.getByLabelText('Abertura'))
  await user.type(screen.getByLabelText('Abertura'), '09:00')
  await user.clear(screen.getByLabelText('Fechamento'))
  await user.type(screen.getByLabelText('Fechamento'), '17:30')
  await user.click(screen.getByRole('button', { name: 'Salvar horário' }))

  expect(
    await screen.findByText('Horário de funcionamento atualizado.'),
  ).toBeInTheDocument()
  const [update] = api.sent(UPDATE)
  expect(update.headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(JSON.parse(update.body)).toEqual({
    daysOfWeek: ['MONDAY', 'WEDNESDAY', 'FRIDAY'],
    openingTime: '09:00',
    closingTime: '17:30',
  })
})

test('requires at least one working day', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [SESSION]: manager,
    [FIND]: () =>
      json({
        daysOfWeek: ['MONDAY'],
        openingTime: '08:00',
        closingTime: '18:00',
      }),
  })

  renderApp('/admin/funcionamento')

  await user.click(await screen.findByLabelText('Segunda-feira'))
  await user.click(screen.getByRole('button', { name: 'Salvar horário' }))

  expect(
    await screen.findByText('Selecione pelo menos um dia de funcionamento.'),
  ).toBeInTheDocument()
  expect(api.sent(UPDATE)).toHaveLength(0)
})

test('offers operating hours only to accounts that can manage spaces', async () => {
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
    screen.queryByRole('link', { name: 'Funcionamento' }),
  ).not.toBeInTheDocument()
  expect(
    screen.getByRole('heading', { name: 'Visão geral' }),
  ).toBeInTheDocument()
})
