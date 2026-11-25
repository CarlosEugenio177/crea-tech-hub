import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, test } from 'vitest'
import { fakeApi, json } from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'

const LIST = 'GET /api/v1/admin/reservations'
const SESSION = 'GET /api/v1/admin/auth/me'

function admin(permissions: string[]) {
  return () =>
    json({
      id: 'f62b98c6-e9e0-4e53-bd67-2a5a7eb16b41',
      fullName: 'Operação',
      email: 'ops@example.com',
      role: 'ADMIN',
      permissions,
      passwordChangeRequired: false,
    })
}

test('filters reservations and shows contact details and late cancellations', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [SESSION]: admin(['MANAGE_SPACES']),
    [LIST]: () =>
      json({
        items: [
          {
            protocol: '33a3edb4-3848-4d4d-b5d3-0cdbba265eed',
            spaceName: 'Sala de estudo',
            fullName: 'Mariana Lopes',
            stationNumber: 2,
            startsAt: '2099-03-10T09:00',
            endsAt: '2099-03-10T10:00',
            status: 'CANCELLED',
            lateCancellation: true,
            email: 'visitor@example.com',
            whatsapp: '86999990000',
            reason: 'Estudo',
            notes: '',
          },
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      }),
  })
  renderApp('/admin/reservas')
  expect(
    await screen.findByRole('heading', { name: 'Sala de estudo · Estação 2' }),
  ).toBeInTheDocument()
  expect(screen.getByText(/Cancelamento tardio/)).toBeInTheDocument()
  await user.click(screen.getByText('Ver contato e detalhes'))
  expect(screen.getByText('visitor@example.com')).toBeVisible()
  await user.type(screen.getByLabelText('Nome ou protocolo'), 'Mariana')
  await user.type(screen.getByLabelText('Ambiente'), 'estudo')
  await user.type(screen.getByLabelText('Data da reserva'), '10032099')
  await user.selectOptions(screen.getByLabelText('Situação'), 'CANCELLED')
  await user.click(screen.getByRole('button', { name: 'Filtrar' }))
  await screen.findByRole('heading', { name: 'Sala de estudo · Estação 2' })
  expect(
    Object.fromEntries(new URLSearchParams(api.sent(LIST).at(-1)?.search)),
  ).toMatchObject({
    search: 'Mariana',
    space: 'estudo',
    date: '2099-03-10',
    status: 'CANCELLED',
    page: '0',
  })
})

test('does not request reservation data without the required permission', async () => {
  const api = fakeApi({ [SESSION]: admin(['MANAGE_ACCESS']) })
  renderApp('/admin/reservas')
  expect(
    await screen.findByText(
      'Sua conta não tem acesso às solicitações de reserva.',
    ),
  ).toBeInTheDocument()
  expect(api.sent(LIST)).toHaveLength(0)
})
