import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, expect, test } from 'vitest'
import { fakeApi, json } from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'
import { selectTime, timeControls } from '../../test/timeControls.ts'
import {
  CATALOG,
  DAY_AGENDA,
  wholeRoomAgenda,
  catalog,
} from './reservationFixtures.ts'

const AVAILABILITY =
  'GET /api/v1/spaces/3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca02/availability'

const STATION_AGENDA =
  'GET /api/v1/spaces/3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca01/availability/day'

beforeEach(() => {
  fakeApi({
    [CATALOG]: catalog,
    [DAY_AGENDA]: wholeRoomAgenda,
    [STATION_AGENDA]: wholeRoomAgenda,
  })
})

test('lets the visitor choose an available period from the day agenda and adjust the minutes', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [CATALOG]: catalog,
    [DAY_AGENDA]: () =>
      json({
        closed: false,
        bookingMode: 'WHOLE_SPACE',
        freeWindows: [{ start: '09:15', end: '10:45' }],
        stations: [],
        periods: [
          { start: '08:00', end: '09:15', available: false },
          { start: '09:15', end: '10:45', available: true },
        ],
      }),
  })
  renderApp('/reservar')
  await user.click(
    await screen.findByRole('button', {
      name: 'Ver horários de Sala de podcast',
    }),
  )
  await user.clear(screen.getByLabelText('Data'))
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  expect(await screen.findByText('Indisponível')).toBeVisible()
  expect(
    screen.queryByRole('button', { name: 'Usar horário das 08:00 às 09:15' }),
  ).not.toBeInTheDocument()
  await user.click(screen.getByRole('button', { name: 'Dia seguinte' }))
  expect(screen.getByLabelText('Data')).toHaveValue('11/03/2099')
  const day = screen.getByRole('button', { name: /12 de março de 2099/ })
  await user.click(day)
  expect(day).toHaveAttribute('aria-pressed', 'true')
  expect(screen.getByLabelText('Data')).toHaveValue('12/03/2099')
  await user.click(
    await screen.findByRole('button', {
      name: 'Usar horário das 09:15 às 10:45',
    }),
  )
  expect(timeControls('Início').hour).toHaveValue('09')
  expect(timeControls('Início').minute).toHaveValue('15')
  expect(timeControls('Fim').hour).toHaveValue('10')
  expect(timeControls('Fim').minute).toHaveValue('45')
  expect(timeControls('Início').hour).toHaveFocus()

  await user.selectOptions(timeControls('Fim').hour, '')
  await selectTime(user, 'Fim', '10:22')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))
  expect(
    await screen.findByRole('heading', { name: 'Seus dados' }),
  ).toBeVisible()
  expect(
    api.sent(DAY_AGENDA).filter(({ search }) => search.includes('2099-03-12')),
  ).toHaveLength(1)
  expect(api.sent(AVAILABILITY)).toHaveLength(0)
})

test('offers only valid end times after the chosen start', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')
  await user.click(
    await screen.findByRole('button', {
      name: 'Ver horários de Estação individual',
    }),
  )
  await screen.findByRole('button', { name: 'Usar horário das 08:00 às 18:00' })
  expect(timeControls('Fim').hour).toBeDisabled()
  await selectTime(user, 'Início', '10:58')
  await user.selectOptions(timeControls('Fim').hour, '10')
  expect(timeControls('Fim').minute).toHaveValue('59')
  expect(timeControls('Fim').minute.querySelectorAll('option')).toHaveLength(1)
  expect(
    within(timeControls('Fim').hour).queryByRole('option', { name: '09' }),
  ).not.toBeInTheDocument()
})

test('only offers end times within the space duration limits', async () => {
  const user = userEvent.setup()
  fakeApi({
    [CATALOG]: catalog,
    [DAY_AGENDA]: () =>
      json({
        closed: false,
        bookingMode: 'WHOLE_SPACE',
        bookingDuration: { minMinutes: 30, maxMinutes: 60 },
        periods: [{ start: '08:00', end: '18:00', available: true }],
        freeWindows: [{ start: '08:00', end: '18:00' }],
        stations: [],
      }),
  })
  renderApp('/reservar')
  await user.click(
    await screen.findByRole('button', {
      name: 'Ver horários de Sala de podcast',
    }),
  )
  expect(
    await screen.findByText('Duração de 30 a 60 min por reserva.'),
  ).toBeVisible()
  await user.click(
    screen.getByRole('button', {
      name: 'Escolher a partir de 08:00 no intervalo até 18:00',
    }),
  )
  expect(timeControls('Fim').hour).toHaveValue('09')
  expect(timeControls('Fim').minute).toHaveValue('00')

  await selectTime(user, 'Início', '09:10')
  const end = timeControls('Fim')
  await user.selectOptions(end.hour, '09')
  expect(end.minute).toHaveValue('40')
  expect(
    within(end.minute).queryByRole('option', { name: '39' }),
  ).not.toBeInTheDocument()
  await user.selectOptions(end.hour, '10')
  expect(
    within(end.minute).getByRole('option', { name: '10' }),
  ).toBeInTheDocument()
  expect(
    within(end.minute).queryByRole('option', { name: '11' }),
  ).not.toBeInTheDocument()

  await user.click(screen.getByRole('button', { name: 'Continuar' }))
  expect(
    await screen.findByRole('heading', { name: 'Seus dados' }),
  ).toBeVisible()
})

test('formats dates and accepts exact minutes using the time selectors', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await user.click(
    await screen.findByRole('button', {
      name: 'Ver horários de Estação individual',
    }),
  )
  await user.clear(screen.getByLabelText('Data'))
  await user.type(screen.getByLabelText('Data'), '10032099')
  await selectTime(user, 'Início', '09:15')
  await selectTime(user, 'Fim', '10:45')

  expect(screen.getByLabelText('Data')).toHaveValue('10/03/2099')
  expect(timeControls('Início').hour).toHaveValue('09')
  expect(timeControls('Início').minute).toHaveValue('15')
  expect(timeControls('Fim').hour).toHaveValue('10')
  expect(timeControls('Fim').minute).toHaveValue('45')
})

test('rejects a date that does not exist in the calendar', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await user.click(
    await screen.findByRole('button', {
      name: 'Ver horários de Estação individual',
    }),
  )
  await user.clear(screen.getByLabelText('Data'))
  await user.type(screen.getByLabelText('Data'), '31/02/2099')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))

  expect(
    await screen.findByText('Informe uma data válida.'),
  ).toBeInTheDocument()
})

test('shows blocked stations and can choose a free station automatically', async () => {
  const user = userEvent.setup()
  const api = fakeApi({
    [CATALOG]: catalog,
    [DAY_AGENDA]: () =>
      json({
        closed: false,
        bookingMode: 'PER_STATION',
        periods: [
          { start: '08:00', end: '10:00', available: true },
          { start: '10:00', end: '18:00', available: true },
        ],
        freeWindows: [],
        stations: [
          {
            id: 'a',
            number: 1,
            x: 20,
            y: 50,
            freeWindows: [{ start: '08:00', end: '10:00' }],
          },
          {
            id: 'b',
            number: 2,
            x: 60,
            y: 50,
            freeWindows: [{ start: '09:00', end: '18:00' }],
          },
        ],
      }),
  })
  renderApp('/reservar')
  await user.click(
    await screen.findByRole('button', {
      name: 'Ver horários de Sala de podcast',
    }),
  )
  await user.clear(screen.getByLabelText('Data'))
  await user.type(screen.getByLabelText('Data'), '10/03/2099')
  await screen.findByRole('button', { name: 'Usar horário das 08:00 às 10:00' })
  await selectTime(user, 'Início', '08:30')
  expect(
    within(timeControls('Fim').hour).queryByRole('option', { name: '11' }),
  ).not.toBeInTheDocument()
  await selectTime(user, 'Início', '09:15')
  await selectTime(user, 'Fim', '10:45')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))
  expect(
    await screen.findByRole('button', { name: 'Estação 1, indisponível' }),
  ).toBeDisabled()
  expect(screen.getByRole('button', { name: 'Estação 2, livre' })).toBeEnabled()
  expect(
    api.sent(DAY_AGENDA).filter(({ search }) => search.includes('2099-03-10')),
  ).toHaveLength(1)
  expect(api.sent(AVAILABILITY)).toHaveLength(0)
  await user.selectOptions(timeControls('Fim').hour, '')
  expect(
    screen.queryByRole('button', {
      name: 'Escolher uma estação livre pra mim',
    }),
  ).not.toBeInTheDocument()
  await selectTime(user, 'Fim', '10:45')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))
  await user.click(
    await screen.findByRole('button', {
      name: 'Escolher uma estação livre pra mim',
    }),
  )
  expect(
    await screen.findByRole('heading', { name: 'Seus dados' }),
  ).toBeInTheDocument()
})
