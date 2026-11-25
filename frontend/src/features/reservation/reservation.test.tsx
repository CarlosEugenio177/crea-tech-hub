import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, expect, test } from 'vitest'
import {
  CSRF_HEADER,
  CSRF_TOKEN,
  fakeApi,
  json,
  problem,
} from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'

const CATALOG = 'GET /api/v1/spaces'
const RULES = 'GET /api/v1/hub/usage-rules'
const SUBMIT = 'POST /api/v1/reservations'
const RECEIPT = 'GET /api/v1/reservations/receipt'
const CANCEL = 'POST /api/v1/reservations/cancel'
const rules = () =>
  json({
    acceptanceRequired: true,
    generalRules: ['Todas as solicitações passam por análise manual.'],
  })
const AVAILABILITY =
  'GET /api/v1/spaces/3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca02/availability'

const DAY_AGENDA = `${AVAILABILITY}/day`

const catalog = () =>
  json({
    items: [
      {
        id: '3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca01',
        name: 'Estação individual',
        type: 'WORKSTATION',
        capacity: 1,
        description: 'Seu posto com computador, reservado só para você.',
      },
      {
        id: '3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca02',
        name: 'Sala de podcast',
        type: 'ROOM',
        capacity: 4,
        description: 'Um espaço dedicado à gravação de podcast.',
      },
      {
        id: '3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca03',
        name: 'Mesa colaborativa',
        type: 'SHARED_TABLE',
        capacity: 8,
        description: 'Uma mesa ampla para encontros e trabalho em equipe.',
      },
    ],
    page: 0,
    size: 20,
    totalElements: 3,
    totalPages: 1,
  })

beforeEach(() => {
  fakeApi({
    [CATALOG]: catalog,
    [RULES]: rules,
    [AVAILABILITY]: () => json({ status: 'AVAILABLE', stations: [] }),
  })
})

async function chooseSpaceAndPeriod(user: ReturnType<typeof userEvent.setup>) {
  await user.click(
    await screen.findByRole('button', { name: 'Escolher Sala de podcast' }),
  )
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  await user.type(screen.getByLabelText('Início'), '09:15')
  await user.type(screen.getByLabelText('Fim'), '10:45')
  await user.click(screen.getByRole('button', { name: 'Ver disponibilidade' }))
  await user.click(await screen.findByRole('button', { name: 'Continuar' }))
  await screen.findByRole('heading', { name: 'Seus dados' })
}

test('lets the visitor choose an available period from the day agenda and adjust the minutes', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [CATALOG]: catalog,
    [DAY_AGENDA]: () =>
      json({
        closed: false,
        periods: [
          { start: '08:00', end: '09:15', available: false },
          { start: '09:15', end: '10:45', available: true },
        ],
      }),
    [AVAILABILITY]: () => json({ status: 'AVAILABLE', stations: [] }),
  })
  renderApp('/reservar')
  await user.click(
    await screen.findByRole('button', { name: 'Escolher Sala de podcast' }),
  )
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  expect(await screen.findByText('Indisponível')).toBeVisible()
  expect(
    screen.queryByRole('button', { name: 'Usar horário das 08:00 às 09:15' }),
  ).not.toBeInTheDocument()
  await user.click(screen.getByRole('button', { name: 'Dia seguinte' }))
  expect(screen.getByLabelText('Data')).toHaveValue('11/03/2099')
  await user.click(
    await screen.findByRole('button', {
      name: 'Usar horário das 09:15 às 10:45',
    }),
  )
  expect(screen.getByLabelText('Início')).toHaveValue('09:15')
  expect(screen.getByLabelText('Fim')).toHaveValue('10:45')
  expect(screen.getByLabelText('Início')).toHaveFocus()

  await user.clear(screen.getByLabelText('Fim'))
  await user.type(screen.getByLabelText('Fim'), '10:22')
  await user.click(screen.getByRole('button', { name: 'Ver disponibilidade' }))
  expect(
    await screen.findByRole('button', { name: 'Continuar' }),
  ).toBeInTheDocument()
  expect(
    Object.fromEntries(
      new URLSearchParams(api.sent(AVAILABILITY).at(-1)?.search),
    ),
  ).toEqual({
    startsAt: '2099-03-11T09:15',
    endsAt: '2099-03-11T10:22',
  })
})

test('keeps the requester after a conflict and opens the receipt after retrying', async () => {
  const user = userEvent.setup()
  const token = 'a'.repeat(43)
  const receipt = {
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
    [CATALOG]: catalog,
    [RULES]: rules,
    [AVAILABILITY]: () => json({ status: 'AVAILABLE', stations: [] }),
    [SUBMIT]: () => problem(409, 'Esse horário não está mais livre.'),
    [RECEIPT]: () => json(receipt),
  })
  renderApp('/')

  await user.click(screen.getByRole('link', { name: 'Solicitar um espaço' }))
  await chooseSpaceAndPeriod(user)
  await user.selectOptions(
    screen.getByLabelText('Perfil'),
    'Mutualista / profissional do Crea',
  )
  await user.type(screen.getByLabelText('Nome completo'), 'Marina Silva')
  await user.type(screen.getByLabelText('CPF'), '529.982.247-25')
  await user.type(screen.getByLabelText('WhatsApp'), '(86) 99999-0000')
  await user.type(screen.getByLabelText('E-mail'), 'marina@example.com')
  await user.click(screen.getByLabelText('Li e aceito as regras de uso do Hub'))
  await user.click(screen.getByRole('button', { name: 'Revisar solicitação' }))

  expect(
    await screen.findByRole('heading', { name: 'Revise sua solicitação' }),
  ).toBeInTheDocument()
  expect(screen.getByText('Sala de podcast')).toBeInTheDocument()
  expect(screen.getByText('09:15 às 10:45')).toBeInTheDocument()
  expect(screen.getByText('Marina Silva')).toBeInTheDocument()
  expect(screen.getByText('***.***.***-25')).toBeInTheDocument()
  expect(api.sent(SUBMIT)).toHaveLength(0)
  await user.click(
    screen.getByRole('button', { name: 'Enviar solicitação para análise' }),
  )
  await user.click(
    await screen.findByRole('link', {
      name: 'Escolher outro horário ou estação',
    }),
  )
  await user.clear(screen.getByLabelText('Início'))
  await user.type(screen.getByLabelText('Início'), '09:30')
  await user.click(screen.getByRole('button', { name: 'Ver disponibilidade' }))
  await user.click(await screen.findByRole('button', { name: 'Continuar' }))
  expect(await screen.findByLabelText('CPF')).toHaveValue('529.982.247-25')
  expect(screen.getByLabelText('Nome completo')).toHaveValue('Marina Silva')
  api.routes[SUBMIT] = () => json({ receipt, trackingToken: token }, 201)
  await user.click(screen.getByRole('button', { name: 'Revisar solicitação' }))
  await user.click(
    await screen.findByRole('button', {
      name: 'Enviar solicitação para análise',
    }),
  )
  expect(
    await screen.findByRole('heading', { name: 'Pedido recebido' }),
  ).toBeInTheDocument()
  expect(screen.getByText('Pendente de análise')).toBeInTheDocument()
  expect(JSON.parse(api.sent(SUBMIT)[1].body).startsAt).toBe('2099-03-10T09:30')
  expect(JSON.parse(api.sent(SUBMIT)[0].body)).toMatchObject({
    rulesAccepted: true,
    requester: { cpf: '52998224725', whatsapp: '86999990000' },
  })
  expect(api.sent(RECEIPT)[0].headers.get('X-Reservation-Token')).toBe(token)
})

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

test('requires the reason from the external public and the acceptance of the rules', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await chooseSpaceAndPeriod(user)
  await user.selectOptions(screen.getByLabelText('Perfil'), 'Público externo')
  await user.type(screen.getByLabelText('Nome completo'), 'Lucas Azevedo')
  await user.type(screen.getByLabelText('CPF'), '111.111.111-11')
  await user.type(screen.getByLabelText('WhatsApp'), '86999990000')
  await user.type(screen.getByLabelText('E-mail'), 'lucas@example.com')
  await user.click(screen.getByRole('button', { name: 'Revisar solicitação' }))

  expect(
    await screen.findByText('Informe o motivo da reserva.'),
  ).toBeInTheDocument()
  expect(
    screen.getByText('Aceite as regras de uso para continuar.'),
  ).toBeInTheDocument()
  expect(screen.getByText('Informe um CPF válido.')).toBeInTheDocument()
  expect(
    screen.getByRole('heading', { name: 'Seus dados' }),
  ).toBeInTheDocument()
})

test('rejects a period that ends before it starts', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await user.click(
    await screen.findByRole('button', { name: 'Escolher Estação individual' }),
  )
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  await user.type(screen.getByLabelText('Início'), '10:00')
  await user.type(screen.getByLabelText('Fim'), '09:00')
  await user.click(screen.getByRole('button', { name: 'Ver disponibilidade' }))

  expect(
    await screen.findByText('O fim deve ser depois do início.'),
  ).toBeInTheDocument()
})

test('formats the date and times in the Brazilian pattern while typing digits', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await user.click(
    await screen.findByRole('button', { name: 'Escolher Estação individual' }),
  )
  await user.type(screen.getByLabelText('Data'), '10032099')
  await user.type(screen.getByLabelText('Início'), '0915')
  await user.type(screen.getByLabelText('Fim'), '1045')

  expect(screen.getByLabelText('Data')).toHaveValue('10/03/2099')
  expect(screen.getByLabelText('Início')).toHaveValue('09:15')
  expect(screen.getByLabelText('Fim')).toHaveValue('10:45')
})

test('rejects a date that does not exist in the calendar', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await user.click(
    await screen.findByRole('button', { name: 'Escolher Estação individual' }),
  )
  await user.type(screen.getByLabelText('Data'), '31/02/2099')
  await user.type(screen.getByLabelText('Início'), '10:00')
  await user.type(screen.getByLabelText('Fim'), '11:00')
  await user.click(screen.getByRole('button', { name: 'Ver disponibilidade' }))

  expect(
    await screen.findByText('Informe uma data válida.'),
  ).toBeInTheDocument()
})

test('sends visitors who open a later step directly back to the first one', () => {
  renderApp('/reservar/revisao')

  expect(
    screen.getByRole('heading', {
      name: 'Qual espaço acompanha seu próximo projeto?',
    }),
  ).toBeInTheDocument()
})

test('shows blocked stations and can choose a free station automatically', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [CATALOG]: catalog,
    [AVAILABILITY]: () =>
      json({
        status: 'AVAILABLE',
        stations: [
          { id: 'a', number: 1, x: 20, y: 50, available: false },
          { id: 'b', number: 2, x: 60, y: 50, available: true },
        ],
      }),
  })
  renderApp('/reservar')
  await user.click(
    await screen.findByRole('button', { name: 'Escolher Sala de podcast' }),
  )
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  await user.type(screen.getByLabelText('Início'), '09:15')
  await user.type(screen.getByLabelText('Fim'), '10:45')
  await user.click(screen.getByRole('button', { name: 'Ver disponibilidade' }))
  expect(
    await screen.findByRole('button', { name: 'Estação 1, indisponível' }),
  ).toBeDisabled()
  expect(screen.getByRole('button', { name: 'Estação 2, livre' })).toBeEnabled()
  expect(
    new URLSearchParams(api.sent(AVAILABILITY)[0].search).get('startsAt'),
  ).toBe('2099-03-10T09:15')
  await user.clear(screen.getByLabelText('Fim'))
  expect(
    screen.queryByRole('button', {
      name: 'Escolher uma estação livre pra mim',
    }),
  ).not.toBeInTheDocument()
  await user.type(screen.getByLabelText('Fim'), '10:45')
  await user.click(screen.getByRole('button', { name: 'Ver disponibilidade' }))
  await user.click(
    await screen.findByRole('button', {
      name: 'Escolher uma estação livre pra mim',
    }),
  )
  expect(
    await screen.findByRole('heading', { name: 'Seus dados' }),
  ).toBeInTheDocument()
})

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
    await screen.findByRole('button', { name: 'Escolher Sala inicial' }),
  ).toBeInTheDocument()
  await user.click(
    screen.getByRole('button', { name: 'Carregar mais espaços' }),
  )
  expect(
    await screen.findByRole('button', { name: 'Escolher Sala seguinte' }),
  ).toBeInTheDocument()
  expect(
    screen.getByRole('button', { name: 'Escolher Sala inicial' }),
  ).toBeInTheDocument()
  expect(
    screen.queryByRole('button', { name: 'Carregar mais espaços' }),
  ).not.toBeInTheDocument()
  expect(new URLSearchParams(api.sent(CATALOG)[1]?.search).get('page')).toBe(
    '1',
  )
})
