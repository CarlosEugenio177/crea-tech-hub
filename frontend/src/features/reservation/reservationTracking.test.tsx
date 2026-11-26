import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, test } from 'vitest'
import {
  CSRF_HEADER,
  CSRF_TOKEN,
  fakeApi,
  json,
  problem,
} from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'

const RECEIPT = 'GET /api/v1/reservations/receipt'

const CANCEL = 'POST /api/v1/reservations/cancel'

test('cancels a reservation from the individual link and shows confirmation', async () => {
  const user = userEvent.setup()
  const token = 'c'.repeat(43)
  const baseReceipt = {
    protocol: '33a3edb4-3848-4d4d-b5d3-0cdbba265eed',
    spaceName: 'Sala de podcast',
    startsAt: '2099-03-10T09:15:00',
    endsAt: '2099-03-10T10:45:00',
    status: 'PENDING',
    cancellable: true,
    lateCancellationWarning: false,
    lateCancellation: false,
  }
  const api = fakeApi({
    [RECEIPT]: () => json(baseReceipt),
    [CANCEL]: () =>
      json({
        ...baseReceipt,
        status: 'CANCELLED',
        cancellable: false,
        cancelledAt: '2026-11-25T13:00:00Z',
      }),
  })

  renderApp(`/acompanhar#${token}`)

  await user.click(
    await screen.findByRole('button', { name: 'Cancelar reserva' }),
  )
  expect(
    screen.getByText(
      'Deseja cancelar esta reserva? O horário será liberado imediatamente.',
    ),
  ).toBeInTheDocument()
  expect(api.sent(CANCEL)).toHaveLength(0)

  await user.click(screen.getByRole('button', { name: 'Manter reserva' }))
  expect(
    screen.queryByRole('button', { name: 'Confirmar cancelamento' }),
  ).not.toBeInTheDocument()

  await user.click(screen.getByRole('button', { name: 'Cancelar reserva' }))
  await user.click(
    screen.getByRole('button', { name: 'Confirmar cancelamento' }),
  )

  expect(
    await screen.findByRole('heading', { name: 'Reserva cancelada' }),
  ).toBeInTheDocument()
  expect(
    screen.getByText('Reserva cancelada com sucesso. O horário foi liberado.'),
  ).toBeInTheDocument()
  expect(screen.getByText('Cancelada')).toBeInTheDocument()
  expect(
    screen.queryByRole('button', { name: 'Cancelar reserva' }),
  ).not.toBeInTheDocument()
  expect(api.sent(CANCEL)[0].headers.get('X-Reservation-Token')).toBe(token)
  expect(api.sent(CANCEL)[0].headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(JSON.parse(api.sent(CANCEL)[0].body)).toEqual({
    confirmLateCancellation: false,
  })
})

test('shows the message from the team on the private tracking page', async () => {
  const token = 'a'.repeat(43)
  const api = fakeApi({
    [RECEIPT]: () =>
      json({
        protocol: '33a3edb4-3848-4d4d-b5d3-0cdbba265eed',
        spaceName: 'Sala de estudo',
        startsAt: '2099-03-10T09:00',
        endsAt: '2099-03-10T10:00',
        status: 'REJECTED',
        cancellable: false,
        lateCancellationWarning: false,
        lateCancellation: false,
        decisionNote: 'A sala estará em manutenção.',
      }),
  })
  renderApp(`/acompanhar#${token}`)
  expect(
    await screen.findByRole('heading', { name: 'Solicitação recusada' }),
  ).toBeInTheDocument()
  expect(screen.getByText('A sala estará em manutenção.')).toBeVisible()
  expect(api.sent(RECEIPT)[0].headers.get('X-Reservation-Token')).toBe(token)
})

test('warns before confirming a late cancellation within 24 hours', async () => {
  const user = userEvent.setup()
  const token = 'd'.repeat(43)
  const baseReceipt = {
    protocol: '33a3edb4-3848-4d4d-b5d3-0cdbba265eed',
    spaceName: 'Coworking',
    stationNumber: 3,
    startsAt: '2099-03-10T09:15:00',
    endsAt: '2099-03-10T10:45:00',
    status: 'CONFIRMED',
    cancellable: true,
    lateCancellationWarning: true,
    lateCancellation: false,
  }
  const api = fakeApi({
    [RECEIPT]: () => json(baseReceipt),
    [CANCEL]: () =>
      json({
        ...baseReceipt,
        status: 'CANCELLED',
        cancellable: false,
        lateCancellationWarning: false,
        lateCancellation: true,
        cancelledAt: '2026-11-25T13:00:00Z',
      }),
  })

  renderApp(`/acompanhar#${token}`)

  expect(
    await screen.findByRole('heading', { name: 'Reserva confirmada' }),
  ).toBeInTheDocument()
  await user.click(screen.getByRole('button', { name: 'Cancelar reserva' }))
  expect(
    screen.getByText(
      'Faltam menos de 24 horas para o início. Este cancelamento será registrado como tardio.',
    ),
  ).toBeInTheDocument()
  expect(api.sent(CANCEL)).toHaveLength(0)

  await user.click(
    screen.getByRole('button', { name: 'Confirmar cancelamento' }),
  )

  expect(
    await screen.findByText(
      'Reserva cancelada com menos de 24 horas de antecedência (registrada como tardia). O horário foi liberado.',
    ),
  ).toBeInTheDocument()
  expect(
    screen.getByText('Cancelada (tardia — menos de 24 h)'),
  ).toBeInTheDocument()
  expect(JSON.parse(api.sent(CANCEL)[0].body)).toEqual({
    confirmLateCancellation: true,
  })
})

test('re-prompts with the late warning if the reservation enters the 24-hour window before confirmation', async () => {
  const user = userEvent.setup()
  const token = 'e'.repeat(43)
  let lateNow = false
  const baseReceipt = {
    protocol: '33a3edb4-3848-4d4d-b5d3-0cdbba265eed',
    spaceName: 'Sala de podcast',
    startsAt: '2099-03-10T09:15:00',
    endsAt: '2099-03-10T10:45:00',
    status: 'PENDING',
    cancellable: true,
    lateCancellation: false,
  }
  const api = fakeApi({
    [RECEIPT]: () => json({ ...baseReceipt, lateCancellationWarning: lateNow }),
    [CANCEL]: () => {
      if (!lateNow) {
        lateNow = true
        return problem(
          409,
          'Faltam menos de 24 horas para o início da reserva. Confirme o cancelamento tardio para continuar.',
        )
      }
      return json({
        ...baseReceipt,
        status: 'CANCELLED',
        cancellable: false,
        lateCancellationWarning: false,
        lateCancellation: true,
        cancelledAt: '2026-11-25T13:00:00Z',
      })
    },
  })

  renderApp(`/acompanhar#${token}`)

  await user.click(
    await screen.findByRole('button', { name: 'Cancelar reserva' }),
  )
  await user.click(
    screen.getByRole('button', { name: 'Confirmar cancelamento' }),
  )

  expect(await screen.findByRole('alert')).toHaveTextContent(
    'Faltam menos de 24 horas para o início da reserva.',
  )
  expect(
    await screen.findByText(
      'Faltam menos de 24 horas para o início. Este cancelamento será registrado como tardio.',
    ),
  ).toBeInTheDocument()

  await user.click(
    screen.getByRole('button', { name: 'Confirmar cancelamento' }),
  )

  expect(
    await screen.findByRole('heading', { name: 'Reserva cancelada' }),
  ).toBeInTheDocument()
  expect(JSON.parse(api.sent(CANCEL)[0].body)).toEqual({
    confirmLateCancellation: false,
  })
  expect(JSON.parse(api.sent(CANCEL)[1].body)).toEqual({
    confirmLateCancellation: true,
  })
})

test('does not accept the public protocol UUID alone on the tracking page', () => {
  const api = fakeApi({})
  renderApp('/acompanhar#33a3edb4-3848-4d4d-b5d3-0cdbba265eed')

  expect(
    screen.getByRole('heading', { name: 'Acompanhe pelo link individual' }),
  ).toBeInTheDocument()
  expect(
    screen.queryByRole('button', { name: 'Cancelar reserva' }),
  ).not.toBeInTheDocument()
  expect(api.sent(RECEIPT)).toHaveLength(0)
})
