import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, test } from 'vitest'
import {
  CSRF_HEADER,
  CSRF_TOKEN,
  fakeApi,
  json,
  noContent,
} from '../../test/fakeApi.ts'
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
            emailDeliveryStatus: 'FAILED',
            email: 'visitor@example.com',
            whatsapp: '86999990000',
            reason: 'Estudo',
            profile: 'EXTERNAL',
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
  expect(screen.getByText('Falha no envio')).toBeVisible()
  await user.click(screen.getByText('Filtrar reservas'))
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

test('sends an administrative decision and refreshes the queue', async () => {
  const user = userEvent.setup()
  const protocol = '33a3edb4-3848-4d4d-b5d3-0cdbba265eed'
  const route = `POST /api/v1/admin/reservations/${protocol}/decisions`
  let status = 'PENDING'
  const api = fakeApi({
    [SESSION]: admin(['MANAGE_SPACES']),
    [LIST]: () =>
      json({
        items: [
          {
            protocol,
            status,
            spaceName: 'Sala de estudo',
            fullName: 'Mariana Lopes',
            startsAt: '2099-03-10T09:00',
            endsAt: '2099-03-10T10:00',
            lateCancellation: false,
            email: 'visitor@example.com',
            whatsapp: '86999990000',
            profile: 'EXTERNAL',
            reason: 'Estudo',
            notes: '',
          },
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      }),
    [route]: () => {
      status = 'CONFIRMED'
      return noContent()
    },
  })
  renderApp('/admin/reservas')
  await user.click(await screen.findByText('Ver contato e detalhes'))
  expect(screen.getByText('Público externo')).toBeVisible()
  expect(screen.getByText('Estudo')).toBeVisible()
  await user.selectOptions(
    screen.getByLabelText('Decisão da equipe'),
    'CONFIRM',
  )
  await user.type(
    screen.getByLabelText('Mensagem para o solicitante (opcional)'),
    'Aguardamos você',
  )
  await user.click(screen.getByRole('button', { name: 'Confirmar reserva' }))
  expect(
    await within(screen.getByRole('listitem')).findByText('Confirmada'),
  ).toBeInTheDocument()
  expect(api.sent(route)[0].headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(JSON.parse(api.sent(route)[0].body)).toEqual({
    expectedStatus: 'PENDING',
    action: 'CONFIRM',
    note: 'Aguardamos você',
  })
  expect(
    screen.getByRole('option', { name: 'Cancelar reserva' }),
  ).toBeInTheDocument()
  expect(
    screen.queryByRole('option', { name: 'Recusar pedido' }),
  ).not.toBeInTheDocument()
})

test('records and corrects attendance from an ended reservation', async () => {
  const user = userEvent.setup()
  const protocol = '33a3edb4-3848-4d4d-b5d3-0cdbba265eed'
  const route = `PUT /api/v1/admin/reservations/${protocol}/attendance`
  let noShow = false
  const api = fakeApi({
    [SESSION]: admin(['MANAGE_SPACES']),
    [LIST]: () =>
      json({
        items: [
          {
            protocol,
            status: 'CONFIRMED',
            spaceName: 'Sala de estudo',
            fullName: 'Visitante de teste',
            startsAt: '2025-04-01T10:00',
            endsAt: '2025-04-01T11:00',
            lateCancellation: false,
            email: 'visitor@example.com',
            whatsapp: '86999990000',
            profile: 'EXTERNAL',
            reason: 'Estudo',
            notes: '',
            attendanceEditable: true,
            noShow,
          },
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      }),
    [route]: () => {
      noShow = !noShow
      return noContent()
    },
  })
  renderApp('/admin/reservas')
  await user.click(await screen.findByText('Ver contato e detalhes'))
  await user.click(screen.getByRole('button', { name: 'Registrar falta' }))
  expect(await screen.findByText('Falta registrada')).toBeVisible()
  await user.click(screen.getByRole('button', { name: 'Desfazer falta' }))
  await screen.findByRole('button', { name: 'Registrar falta' })
  expect(screen.queryByText('Falta registrada')).not.toBeInTheDocument()
  expect(api.sent(route).map(({ body }) => JSON.parse(body))).toEqual([
    { noShow: true },
    { noShow: false },
  ])
})

test('lets the team set and remove a block after repeated absences', async () => {
  const user = userEvent.setup()
  const protocol = '33a3edb4-3848-4d4d-b5d3-0cdbba265eed'
  const resource = `/api/v1/admin/reservations/${protocol}/requester-block`
  let requesterBlockedUntil: string | undefined
  const api = fakeApi({
    [SESSION]: admin(['MANAGE_SPACES']),
    [LIST]: () =>
      json({
        items: [
          {
            protocol,
            status: 'CONFIRMED',
            spaceName: 'Sala de estudo',
            fullName: 'Visitante de teste',
            startsAt: '2025-04-01T10:00',
            endsAt: '2025-04-01T11:00',
            lateCancellation: false,
            email: 'visitor@example.com',
            whatsapp: '86999990000',
            profile: 'EXTERNAL',
            reason: '',
            notes: '',
            attendanceEditable: true,
            noShow: true,
            noShowCount: 2,
            requesterBlockedUntil,
          },
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      }),
    [`PUT ${resource}`]: () => {
      requesterBlockedUntil = '2099-04-10T15:00:00Z'
      return noContent()
    },
    [`DELETE ${resource}`]: () => {
      requesterBlockedUntil = undefined
      return noContent()
    },
  })
  renderApp('/admin/reservas')
  await user.click(await screen.findByText('Ver contato e detalhes'))
  expect(screen.getByText('2 faltas registradas')).toBeVisible()
  const submit = screen.getByRole('button', { name: 'Bloquear novas reservas' })
  expect(submit).toBeDisabled()
  await user.type(screen.getByLabelText('Duração do bloqueio em dias'), '3')
  await user.click(submit)
  expect(await screen.findByText(/^Bloqueado até/)).toBeVisible()
  expect(JSON.parse(api.sent(`PUT ${resource}`)[0].body)).toEqual({
    durationDays: 3,
  })
  await user.click(screen.getByRole('button', { name: 'Remover bloqueio' }))
  await screen.findByRole('button', { name: 'Bloquear novas reservas' })
  expect(api.sent(`DELETE ${resource}`)).toHaveLength(1)
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
