import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, expect, test } from 'vitest'
import { fakeApi, json } from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'

const CATALOG = 'GET /api/v1/spaces'

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
  fakeApi({ [CATALOG]: catalog })
})

async function chooseSpaceAndPeriod(user: ReturnType<typeof userEvent.setup>) {
  await user.click(
    await screen.findByRole('button', { name: 'Escolher Sala de podcast' }),
  )
  await user.type(screen.getByLabelText('Data'), '2099-03-10')
  await user.type(screen.getByLabelText('Início'), '09:15')
  await user.type(screen.getByLabelText('Fim'), '10:45')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))
  await screen.findByRole('heading', { name: 'Seus dados' })
}

test('walks from the home page to the review without sending the request', async () => {
  const user = userEvent.setup()
  renderApp('/')

  await user.click(screen.getByRole('link', { name: 'Solicitar um espaço' }))
  await chooseSpaceAndPeriod(user)
  await user.selectOptions(
    screen.getByLabelText('Perfil'),
    'Mutualista / profissional do Crea',
  )
  await user.type(screen.getByLabelText('Nome completo'), 'Marina Silva')
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
  expect(
    screen.getByRole('button', { name: 'Enviar solicitação para análise' }),
  ).toBeDisabled()
})

test('requires the reason from the external public and the acceptance of the rules', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await chooseSpaceAndPeriod(user)
  await user.selectOptions(screen.getByLabelText('Perfil'), 'Público externo')
  await user.type(screen.getByLabelText('Nome completo'), 'Lucas Azevedo')
  await user.type(screen.getByLabelText('WhatsApp'), '86999990000')
  await user.type(screen.getByLabelText('E-mail'), 'lucas@example.com')
  await user.click(screen.getByRole('button', { name: 'Revisar solicitação' }))

  expect(
    await screen.findByText('Informe o motivo da reserva.'),
  ).toBeInTheDocument()
  expect(
    screen.getByText('Aceite as regras de uso para continuar.'),
  ).toBeInTheDocument()
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
  await user.type(screen.getByLabelText('Data'), '2099-03-10')
  await user.type(screen.getByLabelText('Início'), '10:00')
  await user.type(screen.getByLabelText('Fim'), '09:00')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))

  expect(
    await screen.findByText('O fim deve ser depois do início.'),
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

test('shows an error when the public catalog cannot be loaded', async () => {
  fakeApi({
    [CATALOG]: () =>
      json({ status: 503, detail: 'Catálogo indisponível.' }, 503),
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
