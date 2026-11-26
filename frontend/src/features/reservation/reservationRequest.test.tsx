import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, expect, test } from 'vitest'
import { fakeApi, json, problem } from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'
import { selectTime } from '../../test/timeControls.ts'
import {
  CATALOG,
  DAY_AGENDA,
  wholeRoomAgenda,
  catalog,
} from './reservationFixtures.ts'

const RULES = 'GET /api/v1/hub/usage-rules'
const SUBMIT = 'POST /api/v1/reservations'
const RECEIPT = 'GET /api/v1/reservations/receipt'

const rules = () =>
  json({
    acceptanceRequired: true,
    generalRules: ['Todas as solicitações passam por análise manual.'],
  })

beforeEach(() => {
  fakeApi({
    [CATALOG]: catalog,
    [RULES]: rules,
    [DAY_AGENDA]: wholeRoomAgenda,
  })
})

async function chooseSpaceAndPeriod(user: ReturnType<typeof userEvent.setup>) {
  await user.click(
    await screen.findByRole('button', {
      name: 'Ver horários de Sala de podcast',
    }),
  )
  await user.clear(screen.getByLabelText('Data'))
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  await selectTime(user, 'Início', '09:15')
  await selectTime(user, 'Fim', '10:45')
  await user.click(await screen.findByRole('button', { name: 'Continuar' }))
  await screen.findByRole('heading', { name: 'Seus dados' })
}

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
    [DAY_AGENDA]: wholeRoomAgenda,
    [SUBMIT]: () => problem(409, 'Esse horário não está mais livre.'),
    [RECEIPT]: () => json(receipt),
  })
  renderApp('/')

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
  await selectTime(user, 'Início', '09:30')
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

test('sends visitors who open a later step directly back to the first one', () => {
  renderApp('/reservar/revisao')

  expect(
    screen.getByRole('heading', {
      name: 'Escolha um espaço',
    }),
  ).toBeInTheDocument()
})
