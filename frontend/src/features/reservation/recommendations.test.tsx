import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, test } from 'vitest'
import { fakeApi, json } from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'
import { selectTime, timeControls } from '../../test/timeControls.ts'

const catalog = 'GET /api/v1/spaces'
const recommendations = 'GET /api/v1/spaces/recommendations'
const suggested = {
  id: '33a3edb4-3848-4d4d-b5d3-0cdbba265eed',
  type: 'ROOM',
  name: 'Sala de reunião',
  capacity: 4,
  bookingMode: 'WHOLE_SPACE',
}
const alternative = {
  ...suggested,
  id: '9d4c2c1a-4b11-4c22-8d33-000000000001',
  name: 'Sala multiuso',
  capacity: 8,
}

async function fillCriteria(
  user: ReturnType<typeof userEvent.setup>,
  participants: string,
) {
  await user.type(screen.getByLabelText('Número de pessoas'), participants)
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  await selectTime(user, 'Início', '09:15')
  await selectTime(user, 'Fim', '10:45')
}

test('recommends a room and rechecks the same interval after choosing an alternative', async () => {
  const user = userEvent.setup()
  const availability = `GET /api/v1/spaces/${alternative.id}/availability`
  const api = fakeApi({
    [catalog]: () =>
      json({
        items: [suggested, alternative],
        page: 0,
        size: 100,
        totalPages: 1,
        totalElements: 2,
      }),
    [recommendations]: () =>
      json({
        status: 'AVAILABLE',
        recommended: suggested,
        spaces: [suggested, alternative],
      }),
    [`${availability}/day`]: () =>
      json({
        closed: false,
        bookingMode: 'WHOLE_SPACE',
        freeWindows: [{ start: '08:00', end: '18:00' }],
        stations: [],
        periods: [{ start: '08:00', end: '18:00', available: true }],
      }),
  })

  renderApp('/reservar')
  await user.click(
    await screen.findByRole('link', { name: 'Buscar por número de pessoas' }),
  )
  await fillCriteria(user, '3')
  await user.click(screen.getByRole('button', { name: 'Buscar espaços' }))
  expect(await screen.findByText('Recomendado')).toBeVisible()
  expect(
    Object.fromEntries(
      new URLSearchParams(api.sent(recommendations)[0].search),
    ),
  ).toEqual({
    participants: '3',
    startsAt: '2099-03-10T09:15',
    endsAt: '2099-03-10T10:45',
  })
  await user.click(
    screen.getByRole('button', { name: 'Escolher Sala multiuso' }),
  )
  await screen.findByRole('button', { name: 'Usar horário das 08:00 às 18:00' })
  expect(screen.getByLabelText('Data')).toHaveValue('10/03/2099')
  expect(timeControls('Início').hour).toHaveValue('09')
  expect(timeControls('Início').minute).toHaveValue('15')
  expect(timeControls('Fim').hour).toHaveValue('10')
  expect(timeControls('Fim').minute).toHaveValue('45')
  expect(api.sent(`${availability}/day`)).toHaveLength(1)
  expect(api.sent(availability)).toHaveLength(0)

  await user.click(screen.getByRole('link', { name: 'Voltar às sugestões' }))
  expect(await screen.findByText('Recomendado')).toBeVisible()
  expect(screen.getByLabelText('Número de pessoas')).toHaveValue('3')
})

test('lets the visitor adjust participants when there is no suggestion', async () => {
  const user = userEvent.setup()
  let calls = 0
  const message = 'Ajuste a data, o horário ou o número de pessoas.'
  const api = fakeApi({
    [recommendations]: () =>
      ++calls === 1
        ? json({ status: 'UNAVAILABLE', message, spaces: [] })
        : json({
            status: 'AVAILABLE',
            recommended: suggested,
            spaces: [suggested],
          }),
  })
  renderApp('/reservar/recomendacao')
  await fillCriteria(user, '0')
  await user.click(screen.getByRole('button', { name: 'Buscar espaços' }))
  expect(
    await screen.findByText('Informe pelo menos uma pessoa.'),
  ).toBeVisible()
  expect(api.sent(recommendations)).toHaveLength(0)

  const participants = screen.getByLabelText('Número de pessoas')
  await user.clear(participants)
  await user.type(participants, '20')
  await user.click(screen.getByRole('button', { name: 'Buscar espaços' }))
  expect(await screen.findByText(message)).toBeVisible()
  await user.clear(participants)
  expect(screen.queryByText(message)).not.toBeInTheDocument()
  await user.type(participants, '2')
  await user.click(screen.getByRole('button', { name: 'Buscar espaços' }))
  expect(
    await screen.findByRole('button', { name: 'Escolher Sala de reunião' }),
  ).toBeVisible()
})
